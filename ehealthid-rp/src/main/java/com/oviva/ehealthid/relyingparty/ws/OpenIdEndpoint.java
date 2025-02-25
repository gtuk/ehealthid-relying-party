package com.oviva.ehealthid.relyingparty.ws;

import com.nimbusds.jose.jwk.ECKey;
import com.nimbusds.jose.jwk.JWKSet;
import com.oviva.ehealthid.relyingparty.cfg.RelyingPartyConfig;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.CacheControl;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.net.URI;
import java.time.Duration;
import java.util.List;
import java.util.function.Supplier;

@Path("/")
public class OpenIdEndpoint {

  private final URI baseUri;
  private final RelyingPartyConfig relyingPartyConfig;
  private final Supplier<ECKey> openIdProviderSigningKey;

  public OpenIdEndpoint(
      URI baseUri,
      RelyingPartyConfig relyingPartyConfig,
      Supplier<ECKey> openIdProviderSigningKey) {
    this.baseUri = baseUri;

    this.relyingPartyConfig = relyingPartyConfig;
    this.openIdProviderSigningKey = openIdProviderSigningKey;
  }

  @GET
  @Path("/.well-known/openid-configuration")
  @Produces(MediaType.APPLICATION_JSON)
  public Response openIdConfiguration() {

    var body =
        new OpenIdConfiguration(
            baseUri.toString(),
            baseUri.resolve("/auth").toString(),
            baseUri.resolve("/auth/token").toString(),
            baseUri.resolve("/jwks.json").toString(),
            List.of("openid"),
            relyingPartyConfig.supportedResponseTypes(),
            List.of("authorization_code"),
            List.of("public"),
            List.of("ES256"),
            List.of(),
            List.of(),
            List.of("private_key_jwt"));

    return Response.ok(body).build();
  }

  @GET
  @Path("/jwks.json")
  @Produces(MediaType.APPLICATION_JSON)
  public Response jwks() {
    var key = openIdProviderSigningKey.get().toPublicJWK();

    var cacheControl = new CacheControl();
    cacheControl.setMaxAge((int) Duration.ofMinutes(30).getSeconds());

    return Response.ok(new JWKSet(List.of(key))).cacheControl(cacheControl).build();
  }
}
