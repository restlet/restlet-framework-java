package org.restlet.example.book.restlet.ch08.sec5.common;

import org.restlet.resource.Delete;
import org.restlet.resource.Get;
import org.restlet.resource.Put;

/**
 * User account resource.
 */
public interface AccountResource {

    /**
     * Represents the account as a simple string with the owner name for now.
     * 
     * @return The account representation.
     */
    @Get
    public AccountRepresentation represent();

    /**
     * Stores the new value for the identified account.
     * 
     * @param account
     *            The identified account.
     */
    @Put
    public void store(AccountRepresentation account);

    /**
     * Deletes the identified account by setting its value to null.
     */
    @Delete
    public void remove();

}
