package org.hyf.movie.aspects;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class SecurityAspect {
/*
    @Around(value = "execution(* org.hyf.movie.controller.*.*(..))" )
    public void checkIfLoggedIn(ProceedingJoinPoint joinPoint) throws Throwable {
//        if(are you logged in?)
        //CHeck if the request has the bearer token?
        //  check if the JWT is OK / readable/ contains info that we need etc.
        //OR
        //Check the sessionID cookie on the request and ask to our session
        // table if that sessionID is there
        boolean LOGGEDIN = true;
        if (LOGGEDIN) {
            joinPoint.proceed();
        }
        //else return 401;
    }

    //check if the user has the rights to acces this resource
    //authorization aspect

    çkajhdskahldsaldsasdlja.lashdkahsldkjs.laıhdskajlhds
    {
        user:"Jullie",
        role:"ADMIN"    //this one may not be here
            //what if we are exposed ??
    }
    // How to use the practical aspect of aspect on JWT topic

    */
}
