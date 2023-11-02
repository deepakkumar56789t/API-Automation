@SubscriptionTests
Feature: Subscription management

  @Iteration19.3 @SmokeTests
  Scenario Outline: 1.1 - Verify that there is merchant list retrieved for valid PAN
    Given I create the encrypted data for issuer auxiliary for merchant info
    And I create a valid bearer token for subscription service
    And I have the default Subscription headers
    And I have the merchant list request body as defined in "<validRequestBodies>"
    When I post the details to merchant list endpoint
    Then I verify the status code as "200"
    And I verify response is a valid json
    And I verify the get merchant list response body as expected in "<validResponseBodies>"
    And I verify that merchant info entries are created to event and external logs of Common logging service
    Examples:
      | validRequestBodies                | validResponseBodies                |
      | valid/body/valid_request_body_1.1 | valid/body/valid_response_body_1.1 |

  @Iteration19.3
  Scenario Outline: 1.2 - Verify that there is no merchant list retrieved for invalid authorization
    Given I create the encrypted data for issuer auxiliary for merchant info
    And I have the default Subscription headers
    And I have the merchant list request body as defined in "<validRequestBodies>"
    When I post the details to merchant list endpoint
    Then I verify the status code as "401"
    And I verify that no entries are created to event and external logs of Common logging service
    Examples:
      | validRequestBodies                |
      | valid/body/valid_request_body_1.1 |

  @Iteration19.3
  Scenario Outline: 1.3 - Verify that there is no merchant list retrieved for invalid encrypted pan account
    Given I create a valid bearer token for subscription service
    And I have the default Subscription headers
    And I have the merchant list request body as defined in "<invalidRequestBodies>"
    When I post the details to merchant list endpoint
    Then I verify the status code as "400"
    And I verify response is a valid json
    And I verify the LCM error code "<lcmErrorCode>" in the response
    And I verify that merchant info entries are created to event and external logs of Common logging service
    Examples:
      | invalidRequestBodies                  | lcmErrorCode |
      | invalid/body/invalid_request_body_1.3 | 1031         |

  @Iteration19.4 @SmokeTests
  Scenario Outline: 1.4 - Verify that there is stop payment details retrieved for valid payment ID
    Given Prerequisite: I create a valid stop payment details
    And I have the stop payment by id headers as defined in "<validRequestHeaders>"
    And I have the valid path parameters
    When I retrieve the stop payment details by stop payment id
    Then I verify the status code as "200"
    And I verify response is a valid json
    And I verify the retrieve stop payment response body as expected in "<validResponseBodies>"
    And I verify that retrieve stop payment entries are created to event and external logs of Common logging service
    Examples:
      | validRequestHeaders            | validResponseBodies                |
      | valid/headers/valid_header_1.4 | valid/body/valid_response_body_1.4 |

  @Iteration19.4
  Scenario Outline: 1.5 - Verify that there is no stop payment details retrieved for invalid payment ID
    Given I create a valid bearer token for subscription service
    And I have the stop payment by id headers as defined in "<invalidRequestHeaders>"
    And I have the valid path parameters
    When I retrieve the stop payment details by stop payment id
    Then I verify the status code as "400"
    And I verify response is a valid json
    And I verify the LCM error code "<lcmErrorCode>" in the response
    And I verify that retrieve stop payment entries are created to event and external logs of Common logging service
    Examples:
      | invalidRequestHeaders              | lcmErrorCode |
      | invalid/headers/invalid_header_1.5 | 1031         |

  @Iteration19.4
  Scenario Outline: 1.6 - Verify that there is no stop payment details retrieved for invalid authorization
    Given Prerequisite: I create a valid stop payment details
    And I clear the existing bearer token
    And I have the stop payment by id headers as defined in "<validRequestHeaders>"
    And I have the valid path parameters
    When I retrieve the stop payment details by stop payment id
    Then I verify the status code as "401"
    And I verify that no entries are created to event and external logs of Common logging service
    Examples:
      | validRequestHeaders            |
      | valid/headers/valid_header_1.4 |

  @Iteration19.4 @SmokeTests
  Scenario Outline: 1.7 - Verify that there is stop payment created for valid account and merchant details
    Given I create the encrypted data for issuer auxiliary for create stop payment
    And I create a valid bearer token for subscription service
    And I have the default Subscription headers
    And I have the create stop payment request body as defined in "<validRequestBodies>"
    When I post the details to create stop payment endpoint
    Then I verify the status code as "201"
    And Verify that the response has a valid stop payment ID
    And I verify the dates for create stop payment in database
    And I verify that create stop payment entries are created to event and external logs of Common logging service
    Examples:
      | validRequestBodies                |
      | valid/body/valid_request_body_1.7 |

  @Iteration19.4
  Scenario Outline: 1.8 - Verify that there is no stop payment created for invalid account and merchant details
    Given I create the encrypted data for issuer auxiliary for create stop payment
    And I create a valid bearer token for subscription service
    And I have the default Subscription headers
    And I have the create stop payment request body as defined in "<invalidRequestBodies>"
    When I post the details to create stop payment endpoint
    Then I verify the status code as "400"
    And I verify response is a valid json
    And I verify the LCM error code "<lcmErrorCode>" in the response
    And I verify that create stop payment entries are created to event and external logs of Common logging service
    Examples:
      | invalidRequestBodies                    | lcmErrorCode |
      | invalid/body/invalid_request_body_1.8.1 | 1031         |
      | invalid/body/invalid_request_body_1.8.2 | 1031         |

  @Iteration19.4
  Scenario Outline: 1.9 - Verify that there is no stop payment created for invalid authorization
    Given I create the encrypted data for issuer auxiliary for create stop payment
    And I have the default Subscription headers
    And I have the create stop payment request body as defined in "<validRequestBodies>"
    When I post the details to create stop payment endpoint
    Then I verify the status code as "401"
    And I verify that no entries are created to event and external logs of Common logging service
    Examples:
      | validRequestBodies                |
      | valid/body/valid_request_body_1.7 |

  @Iteration19.4 @SmokeTests
  Scenario Outline: 1.10 - Verify that there is stop payment extended for valid payment ID
    Given Prerequisite: I create a valid stop payment details
    And I have the default Subscription headers
    And I have the extend stop payment request body as defined in "<validRequestBodies>"
    And I have the valid path parameters
    When I put the details to extend stop payment endpoint
    Then I verify the status code as "204"
    And I verify response is a valid json
    And I verify the dates for extend stop payment in database
    And I verify that extend stop payment entries are created to event and external logs of Common logging service
    Examples:
      | validRequestBodies                 |
      | valid/body/valid_request_body_1.10 |

  @Iteration19.4
  Scenario Outline: 1.11 - Verify that there is no stop payment extended for invalid payment details
    Given Prerequisite: I create a valid stop payment details
    And I have the default Subscription headers
    And I have the extend stop payment request body as defined in "<invalidRequestBodies>"
    And I have the valid path parameters
    When I put the details to extend stop payment endpoint
    Then I verify the status code as "400"
    And I verify the LCM error code "<lcmErrorCode>" in the response
    And I verify that extend stop payment entries are created to event and external logs of Common logging service
    Examples:
      | invalidRequestBodies                     | lcmErrorCode |
      | invalid/body/invalid_request_body_1.11.1 | 1031         |
      | invalid/body/invalid_request_body_1.11.2 | 1031         |

  @Iteration19.4
  Scenario Outline: 1.12 - Verify that there is stop payment extended for invalid authorization
    Given Prerequisite: I create a valid stop payment details
    And I clear the existing bearer token
    And I have the default Subscription headers
    And I have the extend stop payment request body as defined in "<validRequestBodies>"
    And I have the valid path parameters
    When I put the details to extend stop payment endpoint
    Then I verify the status code as "401"
    And I verify that no entries are created to event and external logs of Common logging service
    Examples:
      | validRequestBodies                 |
      | valid/body/valid_request_body_1.10 |

  @Iteration19.4 @SmokeTests
  Scenario Outline: 1.13 - Verify that there is stop payment retrieved via Search by PAN
    Given I create the encrypted data for issuer auxiliary for search stop payment
    And I create a valid bearer token for subscription service
    And I have the stop payment by id headers as defined in "<validRequestHeaders>"
    And I have the create stop payment request body as defined in "<validRequestBodies>"
    When I post the details to search by PAN stop payment endpoint
    Then I verify the status code as "200"
    And I verify that the response has a valid stop payment list
    And I verify that search stop payment by PAN entries are created to event and external logs of Common logging service
    Examples:
      | validRequestBodies                 | validRequestHeaders            |
      | valid/body/valid_request_body_1.13 | valid/headers/valid_header_1.4 |

  @Iteration19.4 @SmokeTests
  Scenario Outline: 1.14 - Verify that there is no stop payment retrieved via Search by PAN
    Given I create the encrypted data for issuer auxiliary for search stop payment
    And I create a valid bearer token for subscription service
    And I have the stop payment by id headers as defined in "<validRequestHeaders>"
    And I have the create stop payment request body as defined in "<invalidRequestBodies>"
    When I post the details to search by PAN stop payment endpoint
    Then I verify the status code as "400"
    And I verify the LCM error code "<lcmErrorCode>" in the response
    And I verify that search stop payment by PAN entries are created to event and external logs of Common logging service
    Examples:
      | validRequestHeaders            | invalidRequestBodies                     | lcmErrorCode |
      | valid/headers/valid_header_1.4 | invalid/body/invalid_request_body_1.14.1 | 1031         |
      | valid/headers/valid_header_1.4 | invalid/body/invalid_request_body_1.14.2 | 1031         |

  @Iteration19.4 @SmokeTests
  Scenario Outline: 1.15 - Verify that there is no stop payment retrieved via Search by PAN for invalid authorization
    Given I create the encrypted data for issuer auxiliary for search stop payment
    And I have the stop payment by id headers as defined in "<validRequestHeaders>"
    And I have the create stop payment request body as defined in "<validRequestBodies>"
    When I post the details to search by PAN stop payment endpoint
    Then I verify the status code as "401"
    And I verify that no entries are created to event and external logs of Common logging service
    Examples:
      | validRequestBodies                 | validRequestHeaders            |
      | valid/body/valid_request_body_1.13 | valid/headers/valid_header_1.4 |

#  @Iteration19.4 @SmokeTests
#  Scenario Outline: 1.16 - Verify that there is a stop payment cancelled based on valid payment ID
#    Given I establish connection to subscription database
#    Given I create a valid bearer token for subscription service
#    And I have the stop payment by id headers as defined in "<validRequestHeaders>"
#    When I delete the stop payment details based on ID
#    Then I verify the status code as "200"
#    And I verify response is a valid json
#    Examples:
#      | validRequestHeaders             |
#      | valid/headers/valid_header_1.13 |