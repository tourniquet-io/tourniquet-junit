# Measure User Transactions
Usually, the response times for certain interactions on the user interface are part of the non-functional requirements
and should be part of a specification, the tests. First step to measure response times, is to declare user transactions
for which the begin and end timestamp should be recorded. For this purpose, the pageobjects library provides the 
`Transaction` annotation. ElementGroup, including Pages, and Methods can be marked as transaction. The name of the
user transaction can be specified as value of the annotation or it is dynamically derived from the method or
declaring class. 

## Add Transaction Support to Page Object Model
To enable the transaction support, the ElementGroup or Page has to implement `TransactionSupport`. When the 
instance of the element group or page is created, it is automatically enhanced to record the repsonse times
of methods annotated with `Transaction` as well as the time it takes to load the Page, if the Page implementation 
is annotated as well. For all other ElementGroups annotated with `Transaction`, the annotation is only used to 
derive the name of all transaction of that group.

Example for Declaring annoation:

    public class LoginForm implements ElementGroup, TransactionSupport {
         // ...
         
        @Transaction("Login")
        public void pressLogin(){
            loginButton.get().click();
        }
    }
    
Alternatively, you may declare the start and stop point for the response time measurement imperatively invoking
the `txBegin` and `txEnd` method defined in the `TransactionSupport` interface.
 
    public class LoginForm implements ElementGroup, TransactionSupport {
          // ...
          
         public void pressLogin(){
             txBegin("Login");
             loginButton.get().click();
             txEnd("Login");
         }
    }
