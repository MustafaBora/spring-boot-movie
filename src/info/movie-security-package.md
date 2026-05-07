# 🎬 Security Layer

## 🔐 Authentication & Authorization

## Roles

| Role      | Permissions                  |
|-----------|------------------------------|
| **USER**  | View movies                  |
| **ADMIN** | Full movie management (CRUD) |

Roles stored in the `User` entity:

```java
public enum Role 
{
    USER,
    ADMIN
}
```

JWT tokens include:

* `userId`
* `role`
* `sub` (email)

---

# 🔧 Core Security Classes

Below are the 3 most important files that secure the system.

---

# 1️⃣ JwtUtils (Token Generation & Validation)

Generates JWT tokens and validates them using a **Base64-decoded HMAC SHA-256 key**.

```java
@Component
public class JwtUtils 
{
    private final Key key;             // The signing key used to create/validate JWTs
    private final long expiration;     // JWT expiration duration in milliseconds

    public JwtUtils(
            @Value("${app.jwt.secret}") String secret,       // Base64 encoded secret from properties
            @Value("${app.jwt.expiration}") long expiration  // Expiration time from properties
    ) 
    {
        // Decode Base64 string → raw bytes → secure HMAC SHA256 key
        this.key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
        this.expiration = expiration;
    }

    public String generateToken(String subject, Map<String, Object> claims) 
    {
        Date now = new Date();                             // Current time
        Date exp = new Date(now.getTime() + expiration);   // Expiration timestamp

        return Jwts.builder()
                .setClaims(claims)                         // Add extra claims (role, userId)
                .setSubject(subject)                       // The "sub" field (usually email)
                .setIssuedAt(now)                          // Token creation time
                .setExpiration(exp)                        // Token expiration time
                .signWith(key, SignatureAlgorithm.HS256)   // Sign the token with our secure key
                .compact();                                // Convert to final JWT string
    }

    public boolean isValid(String token) 
    {
        try 
        {
            // Check token expiration by parsing the JWT
            Date expiration = parse(token).getBody().getExpiration();
            return expiration.after(new Date());           // Valid if expiration > now
        } 
        catch (JwtException | IllegalArgumentException e)  // Invalid signature, malformed token, expired, etc.
        {
            return false;
        }
    }

    public String getSubject(String token) 
    {
        // Returns the "sub" claim (email)
        return parse(token).getBody().getSubject();
    }

    private Jws<Claims> parse(String token) 
    {
        // Build a parser that uses our signing key
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token);                    // Parses and validates the JWT
    }
}
```

---

# 2️⃣ JwtAuthenticationFilter (Extract User From JWT)

Executed on **every incoming request**.
If a valid token exists, this filter loads the user from DB and attaches them to the Spring Security context.

```java
@Component
public class JwtAuthenticationFilter implements Filter 
{
    private final JwtUtils jwtUtils;             // Used to validate & decode JWTs
    private final UserRepository userRepository; // Load user details from database

    public JwtAuthenticationFilter(JwtUtils jwtUtils, UserRepository userRepository) 
    {
        this.jwtUtils = jwtUtils;
        this.userRepository = userRepository;
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException 
    {
        HttpServletRequest request = (HttpServletRequest) req;
        String header = request.getHeader("Authorization");   // Read Authorization header

        if (header != null && header.startsWith("Bearer "))   // Check JWT format
        {
            String token = header.substring(7);               // Extract actual JWT

            if (jwtUtils.isValid(token))                      // Validate JWT
            {
                String email = jwtUtils.getSubject(token);    // Extract user identity (email)
                User user = userRepository.findByEmail(email).orElse(null);

                if (user != null) 
                {
                    // Build Spring Security authority from user's role
                    var authority = new SimpleGrantedAuthority("ROLE_" + user.getRole().name());

                    // Store authenticated user in Spring Security context
                    UsernamePasswordAuthenticationToken auth =
                        new UsernamePasswordAuthenticationToken(
                                user,                        // Principal = User entity
                                null,                        // No credentials needed here
                                List.of(authority)           // Authorities = ROLE_USER or ROLE_ADMIN
                        );

                    SecurityContextHolder.getContext().setAuthentication(auth);
                }
            }
        }

        chain.doFilter(req, res);  // Continue request processing
    }
}
```

---

# 3️⃣ SecurityConfig (Route Protection)

Defines which routes are public and which require authentication or admin privileges.

```java
@Configuration
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtFilter;   // Our custom JWT filter

    public SecurityConfig(JwtAuthenticationFilter jwtFilter) 
    {
        this.jwtFilter = jwtFilter;
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() 
    {
        return new BCryptPasswordEncoder();            // Encrypt user passwords
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception 
    {

        http.csrf(csrf -> csrf.disable())             // Disable CSRF for JWT-based APIs
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/auth/**").permitAll()               // Public: register/login
                .requestMatchers(HttpMethod.GET, "/movies/**").permitAll() // Public: view movies
                .requestMatchers("/movies/**").hasRole("ADMIN")        // Protected: CRUD (admin only)
                .anyRequest().authenticated()                          // Everything else requires login
            )
            // Run JWT filter before default UsernamePasswordAuthenticationFilter
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
```

---

