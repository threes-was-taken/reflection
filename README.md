# reflection-exercise - KdG College Antwerp

This exercise is mainly about working test driven and learning to work test driven. Eventually we will create our own small "framework" that makes stubs and skeletons of
the provided classtypes. 

## The project
Again, we received a base project with all tests filled in and we had to work test driven to eventually get all tests to pass. This was a whole new way of working since we always
developped without tests. This exercise is also about invocation and reflection in general. I learned how invocation works and what reflection is and also how it works.

I learned the advantages of TDD but it's still new and i don't know if i'll apply it on my personal projects. Maybe if I want to learn the way of working
in preparation of a future job or something. 

## The problems I faced
The first "big" problem I faced was adapting to the way of working since it was a new way of working.

The second problem I faced was understanding how I could get the parameters from an invoked method. 
I first started with the simple types and I solved this problem by looping
the method parameters, in this loop I get every parameter and corresponding object, then I check if the object class is simple (int, string, bool,...) if so I return the pair
with the param name as key and the string value of the object as value.
I also created a helper class where I created a map of primitive types and their wrapper class ( e.g. string - String, boolean - Boolean, int - Integer,... ) with some
check functions to get the wrapper class of the object type so we can instantiate the simple type when creating an object.

The third problem I faced was complex types (custom objects) as a parameter. Luckily there were some examples available where I could get some inspiration. And since i had the
helper class, the hurdle became somewhat smaller.

The fourth problem was the SkeletonInvocationHandler. This was hard to solve in my opinion. I had to restart a lot. The hardest part was getting the arguments needed for
the response message. The easiest was sending the empty reply. :smile:
