package org.restlet.ext.xml.format;

import java.io.Reader;

/**
 * Deals with parsing the resulting stream into a <code>Entry</code> or
 * <code>Feed</code> and converting it to a <code>OEntity</code>. The
 * implementation depends on the format Atom or Json.
 *
 * @param <T>            Atom or json
 * 
 * @author <a href="mailto:onkar.dhuri@synerzip.com">Onkar Dhuri</a>
 *
 * @see Entry
 * @see Feed
 * @see OEntity
 */
public interface FormatParser<T> {

  /**
   * Parses the feed from reader
   *
   * @param reader the reader
   * @return the t
   */
  T parse(Reader reader);

}
