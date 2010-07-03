package org.restlet.example.book.restlet.ch07.sec2.common;

import org.restlet.resource.Get;
import org.restlet.resource.Post;

/**
 * Collection resource containing user accounts.
 */
public interface AccountsResource {

    /**
     * Returns the list of accounts, each one on a separate line.
     * 
     * @return The list of accounts.
     */
    @Get
    public String represent();

    /**
     * Add the given account to the list and returns its position as an
     * identifier.
     * 
     * @param account
     *            The account to add.
     * @return The account identifier.
     */
    @Post
    public String add(String account);

}
