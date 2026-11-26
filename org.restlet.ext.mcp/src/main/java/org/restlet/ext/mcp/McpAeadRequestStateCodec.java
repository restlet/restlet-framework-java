/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.ext.mcp;

import io.modelcontextprotocol.modern.server.McpRequestContext;
import io.modelcontextprotocol.modern.server.RequestStateCodec;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.util.Base64;
import java.util.Objects;
import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.jspecify.annotations.Nullable;

/**
 * Seals the {@code requestState} of {@code input_required} results with AES-256-GCM, for {@link
 * McpServerAdapter#setRequestStateCodec}: unlike the default HMAC codec, the client can't read the
 * state, so the questions asked so far and the answers already given stay the server's. The state
 * is bound to the request's {@link McpRequestBinding#identity identity} (its user, its arguments
 * and its extension method's name param), its method and primitive name, and an expiry: it opens
 * for no other. Instances behind a load balancer need the same key. Experimental.
 */
public final class McpAeadRequestStateCodec implements RequestStateCodec {

    /** The version of the sealed format, also its associated data. */
    private static final byte VERSION = 1;

    private static final int NONCE_BYTES = 12;

    private static final int TAG_BITS = 128;

    private static final SecureRandom RANDOM = new SecureRandom();

    private final SecretKeySpec key;

    private final Duration ttl;

    private final Clock clock;

    private McpAeadRequestStateCodec(byte[] key, Duration ttl, Clock clock) {
        this.key = new SecretKeySpec(sha256(key), "AES");
        this.ttl = ttl;
        this.clock = clock;
    }

    /**
     * Creates a builder.
     *
     * @return The builder.
     */
    public static Builder builder() {
        return new Builder();
    }

    @Override
    public String seal(McpRequestContext ctx, String state) {
        Objects.requireNonNull(state, "state");
        try {
            var plain = new ByteArrayOutputStream();
            try (var out = new DataOutputStream(plain)) {
                out.writeLong(clock.millis() + ttl.toMillis());
                writeString(out, binding(ctx));
                writeString(out, state);
            }
            byte[] nonce = new byte[NONCE_BYTES];
            RANDOM.nextBytes(nonce);
            byte[] sealed = cipher(Cipher.ENCRYPT_MODE, nonce).doFinal(plain.toByteArray());
            byte[] out = new byte[1 + NONCE_BYTES + sealed.length];
            out[0] = VERSION;
            System.arraycopy(nonce, 0, out, 1, NONCE_BYTES);
            System.arraycopy(sealed, 0, out, 1 + NONCE_BYTES, sealed.length);
            return Base64.getUrlEncoder().withoutPadding().encodeToString(out);
        } catch (IOException | GeneralSecurityException e) {
            throw new IllegalStateException("Unable to seal the MCP requestState", e);
        }
    }

    @Override
    public @Nullable String open(McpRequestContext ctx, String sealed) {
        try {
            byte[] in = Base64.getUrlDecoder().decode(sealed);
            if (in.length < 1 + NONCE_BYTES || in[0] != VERSION) {
                return null;
            }
            byte[] nonce = java.util.Arrays.copyOfRange(in, 1, 1 + NONCE_BYTES);
            byte[] plain =
                    cipher(Cipher.DECRYPT_MODE, nonce)
                            .doFinal(in, 1 + NONCE_BYTES, in.length - 1 - NONCE_BYTES);
            try (var data = new DataInputStream(new java.io.ByteArrayInputStream(plain))) {
                long expiry = data.readLong();
                String binding = readString(data);
                String state = readString(data);
                if (expiry < clock.millis()
                        || !MessageDigest.isEqual(
                                binding.getBytes(StandardCharsets.UTF_8),
                                binding(ctx).getBytes(StandardCharsets.UTF_8))) {
                    return null;
                }
                return state;
            }
        } catch (IllegalArgumentException | IOException | GeneralSecurityException e) {
            // Malformed, tampered with or sealed with another key: it doesn't open.
            return null;
        }
    }

    /** What a state is bound to: the request identity, method and primitive name. */
    private static String binding(McpRequestContext ctx) {
        StringBuilder binding = new StringBuilder(McpRequestBinding.identity(ctx));
        for (String part : new String[] {ctx.method(), ctx.primitiveName()}) {
            binding.append(part == null ? "-" : part.length() + ":" + part).append(';');
        }
        return binding.toString();
    }

    private Cipher cipher(int mode, byte[] nonce) throws GeneralSecurityException {
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(mode, key, new GCMParameterSpec(TAG_BITS, nonce));
        cipher.updateAAD(new byte[] {VERSION});
        return cipher;
    }

    private static void writeString(DataOutputStream out, String value) throws IOException {
        byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
        out.writeInt(bytes.length);
        out.write(bytes);
    }

    private static String readString(DataInputStream in) throws IOException {
        int length = in.readInt();
        if (length < 0 || length > in.available()) {
            throw new IOException("Malformed MCP requestState");
        }
        return new String(in.readNBytes(length), StandardCharsets.UTF_8);
    }

    private static byte[] sha256(byte[] value) {
        try {
            return MessageDigest.getInstance("SHA-256").digest(value);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("SHA-256 is unavailable", e);
        }
    }

    /** Builds an {@link McpAeadRequestStateCodec}. */
    public static final class Builder {
        private byte[] key;

        private Duration ttl = Duration.ofMinutes(10);

        private Clock clock = Clock.systemUTC();

        private Builder() {}

        /**
         * Sets the key, at least 32 bytes; its SHA-256 is the AES-256 key. Without it, a random key
         * is generated, so a retry must reach the instance that answered.
         *
         * @param value The key, copied.
         * @return This builder.
         */
        public Builder key(byte[] value) {
            if (value == null || value.length < 32) {
                throw new IllegalArgumentException("The key must have at least 32 bytes");
            }
            key = value.clone();
            return this;
        }

        /**
         * Sets how long a sealed state opens, ten minutes by default.
         *
         * @param value The duration, positive.
         * @return This builder.
         */
        public Builder ttl(Duration value) {
            if (value == null || value.isZero() || value.isNegative()) {
                throw new IllegalArgumentException("The ttl must be positive");
            }
            ttl = value;
            return this;
        }

        /**
         * Sets the clock of the expiry, the system UTC clock by default.
         *
         * @param value The clock.
         * @return This builder.
         */
        public Builder clock(Clock value) {
            clock = Objects.requireNonNull(value, "clock");
            return this;
        }

        /**
         * Builds the codec.
         *
         * @return The codec.
         */
        public McpAeadRequestStateCodec build() {
            byte[] material = key;
            if (material == null) {
                material = new byte[32];
                RANDOM.nextBytes(material);
            }
            return new McpAeadRequestStateCodec(material, ttl, clock);
        }
    }
}
