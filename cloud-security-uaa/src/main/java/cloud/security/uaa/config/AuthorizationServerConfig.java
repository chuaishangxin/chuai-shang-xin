package cloud.security.uaa.config;

import cloud.security.uaa.component.JwtTokenEnhancer;
import cloud.security.uaa.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.token.TokenEnhancer;
import org.springframework.security.oauth2.provider.token.TokenEnhancerChain;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;

import java.util.ArrayList;
import java.util.List;

/**
 * @author wangpq
 * @version 1.0
 * @className AuthorizationServerConfig
 * @description TODO
 * @date 2020/5/27 10:32
 */
@Configuration
@EnableAuthorizationServer
public class AuthorizationServerConfig extends AuthorizationServerConfigurerAdapter {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserService userService;

    @Autowired
//    @Qualifier("redisTokenStore")
    @Qualifier("jwtTokenStore")
    private TokenStore tokenStore;
    @Autowired
    private JwtAccessTokenConverter jwtAccessTokenConverter;

    @Autowired
    private JwtTokenEnhancer jwtTokenEnhancer;

    @Override
    public void configure(AuthorizationServerSecurityConfigurer oauthServer) throws Exception {
        //url:/oauth/token_key,exposes public key for token verification if using JWT tokens
        oauthServer.tokenKeyAccess("permitAll()")
                //url:/oauth/check_token allow check token
                .checkTokenAccess("isAuthenticated()")
                .allowFormAuthenticationForClients();
    }

    @Override
    public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
        clients.inMemory()
                .withClient("client_1")
                .authorizedGrantTypes("client_credentials")
                .scopes("all","read", "write")
                .authorities("client_credentials")
                .accessTokenValiditySeconds(7200)
                .secret(passwordEncoder.encode("123456"))

                .and().withClient("client_2")
                .authorizedGrantTypes("password", "refresh_token")
                .scopes("all","read", "write")
                .accessTokenValiditySeconds(7200)
                .refreshTokenValiditySeconds(10000)
                .authorities("password")
                .secret(passwordEncoder.encode("123456"))

                .and().withClient("client_3")
                .authorities("authorization_code","refresh_token")
                .secret(passwordEncoder.encode("123456"))
                .authorizedGrantTypes("authorization_code")
                .scopes("all","read", "write")
                .accessTokenValiditySeconds(7200)
                .refreshTokenValiditySeconds(10000)
                .redirectUris("http://localhost:8080/callback","http://localhost:8080/signin")

                .and().withClient("client_test")
                .secret(passwordEncoder.encode("123456"))
                .authorizedGrantTypes("all flow")
                .authorizedGrantTypes("authorization_code", "client_credentials", "refresh_token","password", "implicit")
                .redirectUris("http://localhost:8080/callback","http://localhost:8080/signin")
                .scopes("all","read", "write")
                .accessTokenValiditySeconds(7200)
                .refreshTokenValiditySeconds(10000);
    }

    @Override
    public void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception {
        TokenEnhancerChain enhancerChain = new TokenEnhancerChain();
        List<TokenEnhancer> delegates = new ArrayList<>();
        //配置JWT的内容增强器
        delegates.add(jwtTokenEnhancer);
        delegates.add(jwtAccessTokenConverter);
        enhancerChain.setTokenEnhancers(delegates);
        endpoints.authenticationManager(authenticationManager)
                .userDetailsService(userService)
                //配置令牌存储策略
                .tokenStore(tokenStore)
                .accessTokenConverter(jwtAccessTokenConverter)
                .tokenEnhancer(enhancerChain);
    }
}
