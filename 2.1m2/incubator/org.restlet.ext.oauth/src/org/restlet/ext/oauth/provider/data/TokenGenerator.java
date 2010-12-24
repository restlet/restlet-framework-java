package org.restlet.ext.oauth.provider.data;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Class that controlls the generation of code, token and refresh token.
 * 
 * @author Kristoffer Gronowski
 *
 */
public abstract class TokenGenerator {
	 
	private SecureRandom random;

	private long maxTokenTimeSec;
	
	private static int tokens = 1000;
	private int count = 0;

	public TokenGenerator() {
		try {
			random = SecureRandom.getInstance("SHA1PRNG");

		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}

	}
	
	/**
	 * 
	 * @param user the authenticated user that the code is generated for.
	 * @return the generated code
	 */

	public String generateCode(AuthenticatedUser user) {
		StringBuilder raw = new StringBuilder(generate(20));
		raw.append('|').append(System.currentTimeMillis());
		String code = raw.toString();

		user.addCode(code);

		return code;
	}
	
	/**
	 * 
	 * @param user the authenticated user that the token is generated for.
	 * @param expire grater then zero if not unlimited time token.
	 * @return generated Token or ExpireToken if expire was set.
	 */

	public Token generateToken(AuthenticatedUser user, long expire) {
		long individualExp = user.getTokenExpire();
		if( individualExp > 0 ) { expire = individualExp; }
		expire = ( expire <= maxTokenTimeSec )?expire:maxTokenTimeSec;
		
		StringBuilder raw = new StringBuilder(generate(40));
		//raw.append('|').append(System.currentTimeMillis());
		String token = raw.toString();

		Token t = null;
		if( expire != Token.UNLIMITED ) {
			t = new ExpireToken(token, expire, generate(20), user);
		} else { //Unlimited token
			t = new Token(token, user);
		}
		
		return t;
	}
	
	/**
	 * Calculated the number of seconds to expirey.
	 * If unlimited token Long.MAX_VALUE is returned
	 * 
	 * @param token to be calculated
	 * @return delta time to expirey
	 */
	

	public long expiresInSec(Token token) {
		if(token instanceof ExpireToken) {
			ExpireToken et = (ExpireToken) token;
			ScheduledFuture<?> sf = et.getFuture();
			if( sf != null ) return sf.getDelay(TimeUnit.SECONDS);
		}
		return Long.MAX_VALUE; // TODO create unit test
	}

	/**
	 * 
	 * @param maxTokenTimeSec longest period a token should be valid for.
	 */
	public void setMaxTokenTime(long maxTokenTimeSec) {
		this.maxTokenTimeSec = maxTokenTimeSec;
	}
	
	/**
	 * Refreshes a token throwing away the old one and generating a new
	 * 
	 * @param token to be refreshed
	 */

	public void refreshToken(ExpireToken token) {
		//clean up the old one	
		revokeToken(token);
		
		token.expireToken(); 
		
		String newToken = generate(20); // generate new timed token
		token.setToken(newToken); //Add new token to token object
	}

	/**
	 * Exchanges a code for a token.
	 * Used by the web server flow after callback
	 * 
	 * @param code to be exchange for a token.
	 * @param expire if the generated token should have expiry value set.
	 * @return new token
	 * @throws IllegalArgumentException if the code is not valid or found.
	 */
	public abstract Token exchangeForToken(String code, long expire)
			throws IllegalArgumentException;

	/**
	 * Revoke the token so that it should no longer be used.
	 * 
	 * @param token to revoke.
	 */
	public abstract void revokeToken(Token token);
	
	/**
	 * Revoke the expire token so that it should no longer be used.
	 * 
	 * @param token to revoke.
	 */
	public abstract void revokeExpireToken(ExpireToken token);
	
	/**
	 * Try to find a token.
	 * Implementing class will search for it in the data storage.
	 * 
	 * @param token string to search for.
	 * @return Token or ExpireToken object if found or null.
	 */

	public abstract Token findToken(String token) ;

	private String generate(int len) {
		if( count++ > tokens ) {
			count = 0;
			random.setSeed(random.generateSeed(20));
		}
		byte[] token = new byte[len];
		random.nextBytes(token);
		return toHex(token);
	}

	private String toHex(byte[] input) {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < input.length; i++) {
			String d = Integer
					.toHexString(new Byte(input[i]).intValue() & 0xFF);
			if (d.length() == 1)
				sb.append('0');
			sb.append(d);
		}
		return sb.toString();
	}
}
