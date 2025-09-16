package com.santimattius.http

/**
 * Enumerates the standard HTTP methods supported by the client.
 *
 * Each value represents a different HTTP request method as defined in
 * [RFC 7231](https://tools.ietf.org/html/rfc7231) and other relevant specifications.
 *
 * @see <a href="https://developer.mozilla.org/en-US/docs/Web/HTTP/Methods">HTTP request methods - MDN</a>
 */
enum class HttpMethod {
    /**
     * The GET method requests a representation of the specified resource.
     * Requests using GET should only retrieve data.
     */
    Get,

    /**
     * The POST method is used to submit an entity to the specified resource,
     * often causing a change in state or side effects on the server.
     */
    Post,

    /**
     * The DELETE method deletes the specified resource.
     */
    Delete,

    /**
     * The PUT method replaces all current representations of the target resource
     * with the request payload.
     */
    Put,

    /**
     * The OPTIONS method is used to describe the communication options
     * for the target resource.
     */
    Options,

    /**
     * The HEAD method asks for a response identical to that of a GET request,
     * but without the response body.
     */
    Head,

    /**
     * The PATCH method is used to apply partial modifications to a resource.
     */
    Patch;
}