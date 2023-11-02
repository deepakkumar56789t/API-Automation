@CreateAccountTests
Feature: Issuer

@Iteration25.3 @CreateAccount
Scenario Outline:Scenario 1 - Verify that create or register new account is successful based on valid account info
Given I create a valid bearer token for Issuer service
And I have the issuer headers as defined
And I retrieve encrypted payload request body for create or register as defined in "<requestBodyPath>" for "<accountRefType>"
When I post the details to create and Register payload endpoint
Then I verify the status code as "201"
Then I verify the account information as expected in response for create or register for "<accountRefType>"
And I verify that create account info entries are created to event and external logs of common logging service as expected
Examples:
| requestBodyPath                              | accountRefType |
| valid/body/valid_request_body_Create_Account | PAN            |

@Iteration25.3 @CreateAccount
Scenario Outline:Scenario 2 - Verify that create or register new account is unsuccessful based on invalid autorization
Given I have the issuer headers as defined
And I retrieve encrypted payload request body for create or register as defined in "<requestBodyPath>" for "<accountRefType>"
When I post the details to create and Register payload endpoint
Then I verify the status code as "401"
And  I verify that no entries are created to event and external logs of Common logging service
Examples:
| requestBodyPath                              | accountRefType |
| valid/body/valid_request_body_Create_Account | PAN            |

@Iteration25.3 @CreateAccount
Scenario Outline:Scenario 3 - Verify that create or register new account is unsuccessful based on invalid account
Given I create a valid bearer token for Issuer service
And I have the issuer headers as defined
And I retrieve encrypted payload request body for create or register as defined in "<requestBodyPath>" for "<accountRefType>"
When I post the details to create and Register payload endpoint
Then I verify the status code as "400"
And  I verify that no entries are created to event and external logs of Common logging service
Examples:
| requestBodyPath                                 | accountRefType |
| invalid/body/invalid_request_body_Create_Account | PAN            |