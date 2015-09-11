package org.mellowtech.gapi.model

/**
 * Created by msvens on 24/08/15.
 */
case class ClientCredentials(clientId: String, clientSecret: String, token: String, refreshToken: Option[String])

case class TokenResponse(access_token: String, token_type: String, expires_in: Int, id_token: String, refresh_token: Option[String])
