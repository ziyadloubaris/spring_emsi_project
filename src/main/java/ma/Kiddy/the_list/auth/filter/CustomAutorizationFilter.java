package ma.Kiddy.the_list.auth.filter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;
import com.fasterxml.jackson.databind.ObjectMapper;

import ma.Kiddy.the_list.user.model.UserRole;

import  static org.springframework.http.HttpHeaders.AUTHORIZATION;
import  static org.springframework.http.HttpStatus.FORBIDDEN;;


public class CustomAutorizationFilter extends OncePerRequestFilter {

	@Override
	protected void  doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		// TODO Auto-generated method stub
		if (request.getServletPath().equals("/login")) {
			filterChain.doFilter(request, response); 
		}else{
			String authorizationHeader = request.getHeader(AUTHORIZATION);
			if(authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
				try {
					String tokenString = authorizationHeader.substring("Bearer ".length()); 
					Algorithm algorithm= Algorithm.HMAC256("secret".getBytes());
					JWTVerifier verifier = JWT.require(algorithm).build(); 
					DecodedJWT decodedJWT = verifier.verify(tokenString);
					String username = decodedJWT.getSubject();
					String[] roles = decodedJWT.getClaim("roles").asArray(String.class);
					Collection <SimpleGrantedAuthority> authorities = new ArrayList<>();
					for (String role : roles) {
						authorities.add(new SimpleGrantedAuthority(role));
					}
					UsernamePasswordAuthenticationToken authenticationToken =
							new UsernamePasswordAuthenticationToken(username, null, authorities); 
					SecurityContextHolder.getContext().setAuthentication(authenticationToken);
					filterChain.doFilter(request, response);
				}catch(Exception exception) {
					System.err.println("caught error : "+ exception.getMessage());
					response.setHeader("error", exception.getMessage());
//					response.sendError(FORBIDDEN.value());
					Map<String, String> error = new HashMap<>();
					error.put("error_message", exception.getMessage());
					response.setContentType("application/json;charset=UTF-8");
					new ObjectMapper().writeValue(response.getOutputStream(), error); 

					
				}
			}else {
				filterChain.doFilter(request, response);
			}
		};
	}
	

}
