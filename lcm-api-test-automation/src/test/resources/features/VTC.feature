@VTCTests
Feature: VTC management

  @VTC_Comdirect @Iteration24.5 @SmokeTests
  Scenario Outline: 1.1 Verify that enrolling for transaction control is successful for Comdirect issuer
    Given I create a valid bearer token for Issuer service
    And I have the issuer headers as defined
    And I have the VTC request body as defined "<accountType>"
    When I post the details to VTC API endpoint
    Then I verify the status code as "200"
    And I verify response is a valid json
    And I verify that the response has a valid vtc parameters
    And I verify that vtc entries are created to event and external logs of Common logging service
    Examples:
      | accountType |
      | CardID      |
      | Pan         |

  @VTC_Sparekassen @Iteration24.5 @SmokeTests
  Scenario Outline: 1.2 Verify that enrolling for transaction control is successful for Sparekassen issuer
    Given I create a valid bearer token for Issuer service
    And I have the issuer headers as defined
    And I have the VTC request body as defined "<accountType>"
    When I post the details to VTC API endpoint
    Then I verify the status code as "200"
    And I verify response is a valid json
    And I verify that the response has a valid vtc parameters
    And I verify that vtc entries are created to event and external logs of Common logging service
    Examples:
      | accountType |
      | PANID       |

  @VTC_PayAlly @Iteration24.5 @SmokeTests
  Scenario Outline: 1.3 Verify that enrolling for transaction control is successful for PayAlly issuer
    Given I create a valid bearer token for Issuer service
    And I have the issuer headers as defined
    And I have the VTC request body as defined "<accountType>"
    When I post the details to VTC API endpoint
    Then I verify the status code as "200"
    And I verify response is a valid json
    And I verify that the response has a valid vtc parameters
    And I verify that vtc entries are created to event and external logs of Common logging service
    Examples:
      | accountType |
      | Panref      |

  @VTC_Comdirect @Iteration24.5
  Scenario Outline: 1.4 Verify enrolling for transaction control using cardId with incorrect issuer-id,should send error code
    Given I create a valid bearer token for Issuer service
    And I have the issuer headers for "<incorrectIssuer>"
    And I have the VTC request body as defined "<accountType>"
    When I post the details to VTC API endpoint
    Then I verify the status code as "400"
    And I verify the LCM error code "<lcmErrorCode>" in the response
    Examples:
      | accountType | incorrectIssuer               | lcmErrorCode |
      | CARDID      | Sparekassen Sjælland-Fyn Bank | 1012         |

  @VTC_Comdirect @Iteration24.5
  Scenario Outline: 1.5 Verify enrolling for transaction control using cardId with invalid Provider-Id,issuer-Id should send error code
    Given I create a valid bearer token for Issuer service
    And I have the issuer headers as defined
    And I have invalid "<headers>","<invalidvalue>" for header
    And I have the VTC request body as defined "<accountType>"
    When I post the details to VTC API endpoint
    Then I verify the status code as "400"
    And I verify the LCM error code "<lcmErrorCode>" in the response
    Examples:
      | accountType | lcmErrorCode |headers  | invalidvalue |
      | CardID      | 1011         |Provider | invalid      |
      | CardID      | 1011         |Issuer   | invalid      |
      | CardID      | 1014         |Provider | #$%^&&*      |
      | CardID      | 1014         |Issuer   | #$%^&&*      |


  @Iteration24.5
  Scenario Outline: 1.6 Verify enrolling transaction control for CardId for which vtc is not subscribed should send error code
    Given I create a valid bearer token for Issuer service
    And I have the issuer headers for "<issuer>"
    And I have the request body as for which issuer has not opted VTC "<accountType>"
    When I post the details to VTC API endpoint
    Then I verify the status code as "400"
    And I verify response is a valid json
    Examples:
      | accountType | issuer     |
      | CARDID      | IKANO BANK |


