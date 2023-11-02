@IssuerTests
Feature: Issuer

  @Iteration22.1 @GetMerchantListC2P
  Scenario Outline: Scenario 01 - Verify that there is a merchant list received in order to support Click to pay
    Given I create a valid bearer token for Issuer service
    And I have the issuer headers as defined
    And I have the retrieve merchant list request body as defined in "valid/body/valid_request_body_1.1", "<accountType>"
    When I post the details to retrieve C2P merchant list endpoint
    Then I verify the status code as "200"
    And Verify that the response has a valid merchant list
    And I verify that get merchant list entries are created to event and external logs of Common logging service
    And I verify that the response time is under the SLA for "Get Merchant List - C2P" request
    Examples:
      | accountType |
      | LCMID       |
      | PANREF      |

  @Iteration22.1 @GetMerchantListC2P
  Scenario Outline: Scenario 02 - Verify that there is no merchant list retrieved for invalid authorization
    Given I have the issuer headers as defined
    And I have the retrieve merchant list request body as defined in "valid/body/valid_request_body_1.1", "<accountType>"
    When I post the details to retrieve C2P merchant list endpoint
    Then I verify the status code as "401"
    And I verify that no entries are created to event and external logs of Common logging service
    And I verify that the response time is under the SLA for "Get Merchant List - C2P" request
    Examples:
      | accountType |
      | LCMID       |

  @Iteration22.1 @GetMerchantListC2P
  Scenario Outline: Scenario 03 - Verify that there is no merchant list retrieved for invalid encrypted pan account
    Given I create a valid bearer token for Issuer service
    And I have the issuer headers as defined
    And I have the retrieve merchant list request body as defined in "<invalidRequestBodies>", "<accountType>"
    When I post the details to retrieve C2P merchant list endpoint
    Then I verify the status code as "400"
    And I verify response is a valid json
    And I verify the LCM error code "<lcmErrorCode>" in the response
    And I verify that get merchant list entries are created to event and external logs of Common logging service
    And I verify that the response time is under the SLA for "Get Merchant List - C2P" request
    Examples:
      | invalidRequestBodies                    | accountType | lcmErrorCode |
      | invalid/body/invalid_request_body_1.3.1 |             | 1014         |
      | invalid/body/invalid_request_body_1.3.1 | ABC         | 1014         |
      | invalid/body/invalid_request_body_1.3.2 | LCMID       | 1014         |
      | invalid/body/invalid_request_body_1.3.3 | LCMID       | 1014         |
      | invalid/body/invalid_request_body_1.3.4 | LCMID       | 1011         |
      | invalid/body/invalid_request_body_1.3.2 | PANREF      | 1014         |
      | invalid/body/invalid_request_body_1.3.3 | PANREF      | 1014         |
#      | invalid/body/invalid_request_body_1.3.4 | PANREF      | 1012         |

  @Iteration22.1 @EnrolMerchantC2P
  Scenario Outline: Scenario 04 - Verify that there is a merchant enrolled for Click to pay
    Given I create a valid bearer token for Issuer service
    And I have the issuer headers as defined
    And I have enrol to click2pay request body as defined in "valid/body/valid_request_body_1.4", "<accountType>"
    When I post the details to enrol click2pay endpoint
    Then I verify the status code as "200"
    And Verify that the response has a valid account id
    And I verify that enrol to click2pay entries are created to event and external logs of Common logging service
    And I verify that the response time is under the SLA for "Enrol C2P" request
    Examples:
      | accountType |
      | LCMID       |
      | PANREF      |

  @Iteration22.1 @EnrolMerchantC2P
  Scenario Outline: Scenario 05 - Verify that there is no merchant enrolled for invalid authorization
    Given I have the issuer headers as defined
    And I have enrol to click2pay request body as defined in "valid/body/valid_request_body_1.4", "<accountType>"
    When I post the details to enrol click2pay endpoint
    Then I verify the status code as "401"
    And I verify that no entries are created to event and external logs of Common logging service
    And I verify that the response time is under the SLA for "Enrol C2P" request
    Examples:
      | accountType |
      | LCMID       |
      | PANREF      |

  @Iteration22.1 @EnrolMerchantC2P
  Scenario Outline: Scenario 06 - Verify that there is no merchant enrolled for invalid account info
    Given I create a valid bearer token for Issuer service
    And I have the issuer headers as defined
    And I have enrol to click2pay request body as defined in "<invalidRequestBodies>", "<accountType>"
    When I post the details to enrol click2pay endpoint
    Then I verify the status code as "400"
    And I verify response is a valid json
    And I verify the error code "<errorCode>" in the response
    And I verify that enrol to click2pay entries are created to event and external logs of Common logging service
    And I verify that the response time is under the SLA for "Enrol C2P" request
    Examples:
      | invalidRequestBodies                    | accountType | errorCode |
      | invalid/body/invalid_request_body_1.6.1 |             | LCM-1014  |
      | invalid/body/invalid_request_body_1.6.1 | ABC         | LCM-1014  |
      | invalid/body/invalid_request_body_1.6.2 | LCMID       | LCM-1014  |
      | invalid/body/invalid_request_body_1.6.3 | LCMID       | LCM-1014  |
      | invalid/body/invalid_request_body_1.6.2 | PANREF      | LCM-1014  |
      | invalid/body/invalid_request_body_1.6.3 | PANREF      | LCM-1014  |

  @Iteration22.1 @EnrolMerchantC2P
  Scenario Outline: Scenario 07 - Verify that there is no merchant enrolled for invalid payment instrument provider details
    Given I create a valid bearer token for Issuer service
    And I have the issuer headers as defined
    And I have enrol to click2pay request body as defined in "<invalidRequestBodies>", "<accountType>"
    When I post the details to enrol click2pay endpoint
    Then I verify the status code as "400"
    And I verify response is a valid json
    And I verify the error code "<errorCode>" in the response
    And I verify that enrol to click2pay entries are created to event and external logs of Common logging service
    And I verify that the response time is under the SLA for "Enrol C2P" request
    Examples:
      | invalidRequestBodies                    | accountType | errorCode |
      | invalid/body/invalid_request_body_1.6.4 | LCMID       | LCM-1014  |
      | invalid/body/invalid_request_body_1.6.6 | LCMID       | LCM-1014  |
      | invalid/body/invalid_request_body_1.6.4 | PANREF      | LCM-1014  |
      | invalid/body/invalid_request_body_1.6.6 | PANREF      | LCM-1014  |

  @Iteration22.1 @PushNotificationsC2P
  Scenario Outline: Scenario 08A - Verify that Push provisioning process notification is completed for the given merchant
    Given I create the encrypted data for click2pay push provisioning status
    And Pre-requisite: I enrol a merchant to click2pay feature for "<accountType>"
    When I have the click2pay push provisioning process request body as defined in "valid/body/valid_request_body_1.8" and "<enrolmentStatus>"
    And I post the details to click2pay push provisioning endpoint
    Then I verify the status code as "200"
    And I verify that enrolment status is as expected "<enrolmentStatus>"
    And I verify that the response time is under the SLA for "Push notification C2P" request
    Examples:
      | accountType | enrolmentStatus      |
      | LCMID       | SUCCESS              |
      | PANREF      | SUCCESS              |
      | LCMID       | NOTIFICATION_FAILURE |
      | PANREF      | NOTIFICATION_FAILURE |
      | LCMID       | PROVISION_FAILURE    |
      | PANREF      | PROVISION_FAILURE    |

  @Iteration22.1 @PushNotificationsC2P
  Scenario Outline: Scenario 08B - Verify that Push provisioning process notification is completed when encrypted data is not sent
    Given I create the encrypted data for click2pay push provisioning status
    And Pre-requisite: I enrol a merchant to click2pay feature for "<accountType>"
    When I have the click2pay push provisioning process request body as defined in "<validRequestBodies>" and "<enrolmentStatus>"
    And I post the details to click2pay push provisioning endpoint
    Then I verify the status code as "200"
    And I verify that enrolment status is as expected "<enrolmentStatus>"
    And I verify that the response time is under the SLA for "Push notification C2P" request
    Examples:
      | validRequestBodies                  | accountType | enrolmentStatus |
      | valid/body/valid_request_body_1.8.1 | LCMID       | SUCCESS         |
      | valid/body/valid_request_body_1.8.1 | PANREF      | SUCCESS         |

  @Iteration22.1 @PushNotificationsC2P
  Scenario Outline: Scenario 09A - Verify that Push provisioning process notification is not done for invalid enrolments
    Given I create the encrypted data for click2pay push provisioning status
    And Pre-requisite: I enrol a merchant to click2pay feature for "<accountType>"
    When I have the click2pay push provisioning process request body as defined in "<validRequestBodies>" and "<enrolmentStatus>"
    And I post the details to click2pay push provisioning endpoint
    Then I verify the status code as "200"
    And I verify that enrolment status is as expected "ENROLMENT_FAILURE"
    And I verify that the response time is under the SLA for "Push notification C2P" request
    Examples:
      | validRequestBodies                | accountType | enrolmentStatus |
      | valid/body/valid_request_body_1.8 | LCMID       |                 |
      | valid/body/valid_request_body_1.8 | PANREF      |                 |
      | valid/body/valid_request_body_1.8 | LCMID       | FAILURE         |

  @Iteration22.1 @PushNotificationsC2P
  Scenario Outline: Scenario 09B - Verify that Push provisioning process notification is not done for invalid enrolments
    Given I create the encrypted data for click2pay push provisioning status
    And Pre-requisite: I enrol a merchant to click2pay feature for "<accountType>"
    When I have the click2pay push provisioning process request body as defined in "<validRequestBodies>" and "<enrolmentStatus>"
    And I post the details to click2pay push provisioning endpoint
    Then I verify the status code as "400"
    And I verify that enrolment status still remains same as before
    And I verify that the response time is under the SLA for "Push notification C2P" request
    Examples:
      | validRequestBodies                      | accountType | enrolmentStatus |
      | invalid/body/invalid_request_body_1.8.2 | LCMID       | SUCCESS         |
      | invalid/body/invalid_request_body_1.8.2 | PANREF      | SUCCESS         |

  @Iteration23.4 @GetVirtualAccountInfo
  Scenario Outline: Scenario 10 - Verify that virtual account information is retrieved for valid account and virtual account id
    Given I create a valid bearer token for Issuer service
    And I have the issuer headers as defined
    And I have TSP account id and virtual account ID for "<accountValue>" and "<accountState>" and "<virtualaccountValue>"
    When I retrieve the virtual account information based on virtual account id along with realtime data as "<realTime>"
    Then I verify the status code as "200"
    And I verify the virtual account information on Virtual Account table in database
    And I verify that retrieve virtual account info entries are created to event and external logs of common logging service as expected with realtime data as "<realTime>"
    And I verify that the response time is under the SLA for "Get virtual account info" request
    Examples:
      | accountValue | virtualaccountValue | realTime | accountState |
      | valid        | valid               | false    | ACTIVE       |
      | valid        | valid               | true     | ACTIVE       |

  @Iteration23.4 @GetVirtualAccountInfo
  Scenario Outline: Scenario 11 - Verify that virtual account information is unsuccessful for invalid authorization
    And I have the issuer headers as defined
    And I have TSP account id and virtual account ID for "<accountValue>" and "<accountState>" and "<virtualaccountValue>"
    When I retrieve the virtual account information based on virtual account id along with realtime data as "<realTime>"
    Then I verify the status code as "401"
    And I verify that no entries are created to event and external logs of Common logging service
    And I verify that the response time is under the SLA for "Get virtual account info" request
    Examples:
      | accountValue | virtualaccountValue | realTime | accountState |
      | valid        | valid               | false    | ACTIVE       |

  @Iteration23.4 @GetVirtualAccountInfo
  Scenario Outline: Scenario 12 - Verify that virtual account information is not retrieved for given invalid details
    Given I create a valid bearer token for Issuer service
    And I have the issuer headers as defined
    And I have TSP account id and virtual account ID for "<accountValue>" and "<accountState>" and "<virtualaccountValue>"
    When I retrieve the virtual account information based on virtual account id along with realtime data as "<realTime>"
    Then I verify the status code as "<statusCode>"
    And I verify response is a valid json
    And I verify the LCM error code "<lcmErrorCode>" in the response body
    And I verify that the response time is under the SLA for "Get virtual account info" request
    Examples:
      | accountValue | virtualaccountValue | realTime | accountState | lcmErrorCode | statusCode |
      | valid        | invalid             | false    | ACTIVE       | 1116         | 404        |
      | invalid      | valid               | false    | ACTIVE       | 1012         | 404        |
      | valid        | valid               | test     | ACTIVE       | 1015         | 500        |

  @Iteration23.4 @GetVirtualAccountInfo
  Scenario Outline: Scenario 13 - Verify that virtual account information is not retrieved for empty account or virtual account id
    Given I create a valid bearer token for Issuer service
    And I have the issuer headers as defined
    And I have TSP account id and virtual account ID for "<accountValue>" and "<accountState>" and "<virtualaccountValue>"
    When I retrieve the virtual account information based on virtual account id along with realtime data as "<realTime>"
    Then I verify the status code as "<statusCode>"
    And I verify response is a valid json
    And I verify that no entries are created to event and external logs of Common logging service
    And I verify that the response time is under the SLA for "Get virtual account info" request
    Examples:
      | accountValue | virtualaccountValue | realTime | accountState | statusCode |
      | valid        | null                | false    | ACTIVE       | 404        |
      | null         | valid               | false    | ACTIVE       | 404        |
      | null         | null                | false    | ACTIVE       | 404        |

  @Iteration24.5 @UpdateProfileIdForAccounts
  Scenario Outline: Scenario 14 - Verify that profileid is updated for valid account and its corresponding virtual accounts
    Given I create a valid bearer token for Issuer service
    And I have the issuer headers as defined
    And I fetch "<state>" accountId of provided issuer from database
    And I have the update profile id request body as defined in "valid/body/valid_request_body_UP"
    And I put the request details to profileId service endpoint with virtual account as "<virtualAccountFlag>"
    Then I verify the status code as "204"
    And I verify profile id for account is updated as expected
    And I verify that profile id for virtual accounts is updated as expected when virtual account flag is "<virtualAccountFlag>"
    And I verify that update profile id entries are created to event and external logs of common logging service as expected "<virtualAccountFlag>"
    And I verify that the response time is under the SLA for "Update ProfileId" request
    Examples:
      | virtualAccountFlag | state  |
      | true               | ACTIVE |
      | false              | ACTIVE |

  @Iteration24.5 @UpdateProfileIdForAccounts
  Scenario Outline: Scenario 15 - Verify LCM error code is sent when invalid headers are provided in update profileid service
    Given I create a valid bearer token for Issuer service
    And I have the issuer headers as defined
    And I have invalid issuer headers for "<headers>" and "<invalidvalue>"
    And I fetch "<state>" accountId of provided issuer from database
    And I have the update profile id request body as defined in "valid/body/valid_request_body_UP"
    And I put the request details to profileId service endpoint with virtual account as "<virtualAccountFlag>"
    Then I verify the status code as "400"
    And I verify the LCM error code "<lcmErrorCode>" in the response
    And I verify that update profile id entries are created to event and external logs of common logging service as expected "<virtualAccountFlag>"
    And I verify that the response time is under the SLA for "Update ProfileId" request
    Examples:
      | virtualAccountFlag | lcmErrorCode | headers       | invalidvalue   | state  |
      | true               | 1011         | X-Provider-ID | 1234567890865  | ACTIVE |
      | true               | 1011         | X-Issuer-ID   | XY-12057000700 | ACTIVE |

  @Iteration24.5 @UpdateProfileIdForAccounts
  Scenario Outline: Scenario 16 - Verify LCM error code is sent when invalid account id is provided in update profileid service
    Given I create a valid bearer token for Issuer service
    And I have the issuer headers as defined
    And I provide invalid accountId "<invalidAccount>"
    And I have the update profile id request body as defined in "valid/body/valid_request_body_UP"
    And I put the request details to profileId service endpoint with virtual account as "<virtualAccountFlag>"
    Then I verify the status code as "400"
    And I verify the LCM error code "<lcmErrorCode>" in the response
    And I verify that update profile id entries are created to event and external logs of common logging service as expected "<virtualAccountFlag>"
    And I verify that the response time is under the SLA for "Update ProfileId" request
    Examples:
      | virtualAccountFlag | lcmErrorCode | invalidAccount |
      | true               | 1011         | 00000123453    |

  @Iteration24.5 @UpdateProfileIdForAccounts
  Scenario Outline: Scenario 17 - Verify profileId is not updated for account for which Accounts is in DELETED state
    Given I create a valid bearer token for Issuer service
    When I have the issuer headers as defined
    And I fetch "<state>" accountId of provided issuer from database
    And I have the update profile id request body as defined in "valid/body/valid_request_body_UP"
    And I put the request details to profileId service endpoint with virtual account as "<virtualAccountFlag>"
    Then I verify the status code as "400"
    And I verify profile id for account is not updated
    And I verify that profile id for virtual accounts is updated as expected when virtual account flag is "<virtualAccountFlag>"
    And I verify that update profile id entries are created to event and external logs of common logging service as expected "<virtualAccountFlag>"
    And I verify that the response time is under the SLA for "Update ProfileId" request
    Examples:
      | virtualAccountFlag | state   |
      | true               | DELETED |

  @Iteration25.1 @GetAccountInformationByAccountId
  Scenario Outline: Scenario 18 - Verify that retrieve account information is successful based on valid account id
    Given I create a valid bearer token for Issuer service
    And I have the issuer headers as defined
    And I fetch "<state>" accountId of provided issuer from database
    When I retrieve the account information based on account id
    And I verify the status code as "200"
    Then I verify the account information as expected in response
    And I verify that get account info entries are created to event and external logs of common logging service as expected
    And I verify that the response time is under the SLA for "Get AccountInformation By AccId" request
    Examples:
      | state  |
      | ACTIVE |

  @Iteration25.1 @GetAccountInformationByAccountId
  Scenario Outline: Scenario 19 - Verify that retrieve account information is unsuccessful for missing authorization
    Given I have the issuer headers as defined
    And I fetch "<state>" accountId of provided issuer from database
    When I retrieve the account information based on account id
    And I verify the status code as "401"
    And I verify that no entries are created to event and external logs of Common logging service
    And I verify that the response time is under the SLA for "Get AccountInformation By AccId" request
    Examples:
      | state  |
      | ACTIVE |
#      | DELETED   |
#      | SUSPENDED |

  @Iteration25.1 @GetAccountInformationByAccountId
  Scenario Outline: Scenario 20 - Verify that retrieve account information is unsuccessful for invalid account id
    Given I create a valid bearer token for Issuer service
    And I have the issuer headers as defined
    And I provide invalid accountId "<invalidAccount>"
    When I retrieve the account information based on account id
    Then I verify the status code as "404"
    And I verify response is a valid json
    And I verify the LCM error code "<lcmErrorCode>" in the response
    And I verify that get account info entries are created to event and external logs of common logging service as expected
    And I verify that the response time is under the SLA for "Get AccountInformation By AccId" request
    Examples:
      | lcmErrorCode | invalidAccount |
      | 1012         | 000            |
      | 1012         | 123456789000   |

  @Iteration25.1 @GetAccountIdByAccountInformation
  Scenario Outline: Scenario 21 - Verify that retrieve account id is successful based on valid account information
    Given I create a valid bearer token for Issuer service
    And I have the issuer headers as defined
    And I get account id by account info request body as defined "valid/body/valid_request_body_1.1" for "<accountType>" and "<accountState>"
    When I retrieve the account Id from provided account information
    Then I verify the status code as "200"
    And I verify response is a valid json
    And I verify the account id is as expected in response
    And I verify that get account id entries are created to event and external logs of common logging service as expected
    And I verify that the response time is under the SLA for "Get AccId By AccountInformation" request
    Examples:
      | accountType | accountState |
      | PAN         | ACTIVE       |
    #  | PAN         | DELETED      |
#      | PAN         | SUSPENDED    |

  @Iteration25.1 @GetAccountIdByAccountInformation
  Scenario Outline: Scenario 22 - Verify that retrieve account id is unsuccessful for missing authorization
    Given I have the issuer headers as defined
    And I get account id by account info request body as defined "<validRequestBodies>" for "<accountType>" and "<accountState>"
    When I retrieve the account Id from provided account information
    Then I verify the status code as "401"
    And I verify that no entries are created to event and external logs of Common logging service
    And I verify that the response time is under the SLA for "Get AccId By AccountInformation" request
    Examples:
      | accountType | accountState | validRequestBodies                |
      | PAN         | ACTIVE       | valid/body/valid_request_body_1.1 |
#      | PAN         | DELETED      |valid/body/valid_request_body_1.1|
#      | PAN         | SUSPENDED    |valid/body/valid_request_body_1.1 |

  @Iteration25.1 @GetAccountIdByAccountInformation
  Scenario Outline: Scenario 23 - Verify that retrieve account id is unsuccessful for invalid request
    Given I create a valid bearer token for Issuer service
    And I have the issuer headers as defined
    And I have the invalid issuer request body "invalid/body/invalid_request_body_1.21" for defined "<accountValue>"
    When I retrieve the account Id from provided account information
    Then I verify the status code as "406"
    And I verify the LCM error code "<lcmErrorCode>" in the response
    And I verify that get account id entries are created to event and external logs of common logging service as expected
    And I verify that the response time is under the SLA for "Get AccId By AccountInformation" request
    Examples:
      | accountValue    | lcmErrorCode |
      | 123456789123400 | 1014         |

  @Iteration25.1 @GetEncryptedPayload
  Scenario Outline: Scenario 24 - Verify retrieval of encrypted payload is successful
    Given I create a valid bearer token for Issuer service
    And I have the issuer headers as defined
    And I retrieve encrypted payload request body as defined in "valid/body/valid_request_body_InAppPayload" for "<tokenRequesterName>" and "<accountRefType>"
    When I post the details to inApp payload endpoint
    Then I verify the status code as "200"
    And I verify inApp payload response is as expected for "<tokenRequesterName>"
    And I verify that for get encrypted data entries are created to event and external logs of common logging service as expected
    And I verify that the response time is under the SLA for "Get Encrypted Payload" request
    Examples:
      | tokenRequesterName | accountRefType |
      | Google Pay         | PAN            |
      | Apple pay          | PAN            |

  @Iteration25.1 @GetEncryptedPayload
  Scenario Outline: Scenario 25 - Verify retrieval of encrypted payload is unsuccessful for missing authorization
    Given I have the issuer headers as defined
    And I retrieve encrypted payload request body as defined in "valid/body/valid_request_body_InAppPayload" for "<tokenRequesterName>" and "<accountRefType>"
    When I post the details to inApp payload endpoint
    Then I verify the status code as "401"
    And I verify that no entries are created to event and external logs of Common logging service
    And I verify that the response time is under the SLA for "Get Encrypted Payload" request
    Examples:
      | tokenRequesterName | accountRefType |
      | Google Pay         | PAN            |
      | Apple pay          | PAN            |

  @Iteration25.1 @GetEncryptedPayload
  Scenario Outline: Scenario 26 - Verify retrieval of encrypted payload is unsuccessful for invalid request
    Given I create a valid bearer token for Issuer service
    And I have the issuer headers as defined
    And I retrieve invalid encrypted payload request body as defined in "invalid/body/invalid_request_body_InAppPayload" for "<tokenRequesterName>" and "<accountRefType>" with invalid values as "<invalidAttribute>","<invalidValue>"
    When I post the details to inApp payload endpoint
    Then I verify the status code as "400"
    And I verify that for get encrypted data entries are created to event and external logs of common logging service as expected
    And I verify that the response time is under the SLA for "Get Encrypted Payload" request
    Examples:
      | tokenRequesterName | accountRefType | invalidAttribute | invalidValue |
      | Google Pay         | PAN            | account          | 12345678&00  |
      | Apple pay          | PAN            | accountExpiry    | 99999999     |

  @Iteration25.3 @GetTokensByWalletIdIssuer
  Scenario: Scenario 27 -Verify that retrieve of tokens is successful based on valid walletId
    Given I create a valid bearer token for Issuer service
    And I have the issuer headers as defined
    When I get wallet id for provided issuer
    And I retrieve the tokens based on wallet id
    And I verify the status code as "200"
    Then I verify token details as expected for issuer
    And I verify that get tokens entries are created to event and external logs of common logging service as expected
    And I verify that the response time is under the SLA for "Get Tokens By WalletId" request

  @Iteration25.3 @GetTokensByWalletIdIssuer
  Scenario: Scenario 28 -Verify that retrieve of tokens is unsuccessful for missing authorization
    Given I have the issuer headers as defined
    When I get wallet id for provided issuer
    And I retrieve the tokens based on wallet id
    And I verify the status code as "401"
    And I verify that no entries are created to event and external logs of Common logging service
    And I verify that the response time is under the SLA for "Get Tokens By WalletId" request

  @Iteration25.3 @GetTokensByWalletIdIssuer
  Scenario Outline: Scenario 29 -Verify that retrieve of tokens is unsuccessful based on invalid walletId
    Given I create a valid bearer token for Issuer service
    And I have the issuer headers as defined
    When I fetch tokens with invalid WalletId "<invalidWalletId>"
    And I retrieve the tokens based on wallet id
    And I verify the status code as "404"
    And I verify the LCM error code "<lcmErrorCode>" in the response
    And I verify that get tokens entries are created to event and external logs of common logging service as expected
    And I verify that the response time is under the SLA for "Get Tokens By WalletId" request
    Examples:
      | invalidWalletId      | lcmErrorCode |
      | 12345678901234567890 | 1016         |

  @Iteration25.3 @GetVirtualAccountIdByTPANIssuer
  Scenario: Scenario 30 - Verify that retrieve of virtual account is successful by TPAN
    Given I create a valid bearer token for Issuer service
    And I have the issuer headers as defined
    And I create the encrypted data for TPAN
    And I have the TPAN request body as defined in "valid/body/valid_request_body_TPAN"
    And I post the details to retrieve virtual account id by TPAN endpoint
    And I verify the status code as "200"
    Then I verify the virtual account id as expected
    And I verify that the response time is under the SLA for "Get Virtual Account By TPAN" request

  @Iteration25.3 @GetVirtualAccountIdByTPANIssuer
  Scenario: Scenario 31 - Verify that retrieve of virtual account is unsuccessful by TPAN for missing auth
    Given I have the issuer headers as defined
    And I create the encrypted data for TPAN
    And I have the TPAN request body as defined in "valid/body/valid_request_body_TPAN"
    And I post the details to retrieve virtual account id by TPAN endpoint
    And I verify the status code as "401"
    And I verify that no entries are created to event and external logs of Common logging service
    And I verify that the response time is under the SLA for "Get Virtual Account By TPAN" request

  @Iteration25.3 @GetVirtualAccountIdByTPANIssuer
  Scenario Outline: Scenario 32 - Verify that retrieve of virtual account is unsuccessful by invalid TPAN
    Given I create a valid bearer token for Issuer service
    And I have the issuer headers as defined
    And I create the encrypted data for "<invalidTPAN>" TPAN
    And I have the TPAN request body as defined in "valid/body/valid_request_body_TPAN"
    And I post the details to retrieve virtual account id by TPAN endpoint
    And I verify the status code as "404"
    And I verify the LCM error code "<lcmErrorCode>" in the response
    And I verify that the response time is under the SLA for "Get Virtual Account By TPAN" request
    Examples:
      | invalidTPAN | lcmErrorCode |
      | 1232334512  | 1016         |

  @Iteration25.3 @UpdateAccountStateIssuer
  Scenario Outline: Scenario 33 - Verify that update of account state is successful
    Given I create a valid bearer token for Issuer service
    And I have the issuer headers as defined
    And I have the update account state request body as defined in "valid/body/valid_request_body_AS" and state as "<state>"
    And I fetch account info to update state as "<state>"
    And I update the account state based on account id
    And I verify the status code as "200"
    Then I verify the account state as expected
    And I again RESUME the accountState to make it ACTIVE
    And I verify that update account state entries are created to event and external logs of common logging service as expected
    And I verify that the response time is under the SLA for "Update Account State" request
    Examples:
      | state   |
      | SUSPEND |

  @Iteration25.3 @UpdateAccountStateIssuer
  Scenario Outline: Scenario 34 - Verify that update of account state is unsuccessful for missing authorization
    Given I have the issuer headers as defined
    And I have the update account state request body as defined in "valid/body/valid_request_body_AS" and state as "<state>"
    And I fetch account info to update state as "<state>"
    And I update the account state based on account id
    And I verify the status code as "401"
    And I verify that no entries are created to event and external logs of Common logging service
    And I verify that the response time is under the SLA for "Update Account State" request
    Examples:
      | state   |
      | SUSPEND |

  @Iteration25.3 @UpdateAccountStateIssuer
  Scenario Outline: Scenario 35 - Verify that update of account state is successful
    Given I create a valid bearer token for Issuer service
    And I have the issuer headers as defined
    And I have the update account state request body as defined in "valid/body/valid_request_body_AS" and state as "<state>"
    And I fetch account info to update state as "<state>"
    And I update the account state based on account id
    And I verify the status code as "406"
    And I verify the LCM error code "<lcmErrorCode>" in the response
    And I verify that update account state entries are created to event and external logs of common logging service as expected
    And I verify that the response time is under the SLA for "Update Account State" request
    Examples:
      | state    | lcmErrorCode |
      | Rejected | 1014         |

  @Iteration25.3 @UpdateVirtualAccountStateIssuer
  Scenario Outline: Scenario 36 - Verify that update of virtual account state is successful
    Given I create a valid bearer token for Issuer service
    And I have the issuer headers as defined
    And I have the update virtual account state request body as defined in "valid/body/valid_request_body_VAS" and state as "<state>"
    And I fetch virtual account info to update state as "<state>"
    And I update the virtual account state based on account id
    And I verify the status code as "200"
    Then I verify the virtual account state as expected
    And I again RESUME the virtual accountState to make it ACTIVE
    And I verify that virtual account state entries are created to event and external logs of common logging service as expected
    And I verify that the response time is under the SLA for "Update VirtualAccount State" request
    Examples:
      | state   |
      | SUSPEND |

  @Iteration25.3 @UpdateVirtualAccountStateIssuer
  Scenario Outline: Scenario 37 - Verify that update of virtual account state is unsuccessful with missing authorization
    Given I have the issuer headers as defined
    And I have the update virtual account state request body as defined in "valid/body/valid_request_body_VAS" and state as "<state>"
    And I fetch virtual account info to update state as "<state>"
    And I update the virtual account state based on account id
    And I verify the status code as "401"
    Then I verify that no entries are created to event and external logs of Common logging service
    And I verify that the response time is under the SLA for "Update VirtualAccount State" request
    Examples:
      | state   |
      | SUSPEND |

  @Iteration25.3 @UpdateVirtualAccountStateIssuer
  Scenario Outline: Scenario 38 - Verify that update of virtual account state is successful
    Given I create a valid bearer token for Issuer service
    And I have the issuer headers as defined
    And I have the update virtual account state request body as defined in "valid/body/valid_request_body_VAS" and state as "<state>"
    And I fetch virtual account info to update state as "<state>"
    And I update the virtual account state based on account id
    And I verify the status code as "400"
    And I verify the error code "<errorCode>" in the response
    And I verify that virtual account state entries are created to event and external logs of common logging service as expected
    And I verify that the response time is under the SLA for "Update VirtualAccount State" request
    Examples:
      | state     | errorCode |
      | SUSPENDED | LCM-1014  |

  @Iteration25.3 @UpdateProfileIdofVA
  Scenario Outline: Scenario 39 - Verify that profileId update is successful for virtual Account
    Given I create a valid bearer token for Issuer service
    And I have the issuer headers as defined
    And I fetch virtual accountId "<vaState>" and accountId as "<state>" to update profileId
    And I have the update profile id request body as defined in "valid/body/valid_request_body_UP"
    When I update the profile id based on account id and virtual account id
    And I verify the status code as "200"
    Then I verify the profile id as expected
    And I verify that profile Id update entries are created to event and external logs of common logging service as expected
    And I verify that the response time is under the SLA for "Update VirtualAccount ProfileId" request
    Examples:
      | state  | vaState |
      | ACTIVE | ACTIVE  |
#	  | ACTIVE | SUSPENDED |
#	  | ACTIVE | INACTIVE  |

  @Iteration25.3 @UpdateProfileIdofVA
  Scenario Outline: Scenario 40 - Verify that profileId update is unsuccessful for virtual Account with missing authorization
    Given I have the issuer headers as defined
    And I have the issuer headers as defined
    And I fetch virtual accountId "<vaState>" and accountId as "<state>" to update profileId
    And I have the update profile id request body as defined in "valid/body/valid_request_body_UP"
    When I update the profile id based on account id and virtual account id
    And I verify the status code as "401"
    Then I verify that no entries are created to event and external logs of Common logging service
    And I verify that the response time is under the SLA for "Update VirtualAccount ProfileId" request
    Examples:
      | state  | vaState |
      | ACTIVE | ACTIVE  |

  @Iteration25.3 @UpdateProfileIdofVA
  Scenario Outline: Scenario 41 - Verify that profileId update is unsuccessful for invalid virtual Account
    Given I create a valid bearer token for Issuer service
    And I have the issuer headers as defined
    And I have the update profile id request body as defined in "valid/body/valid_request_body_UP"
    When I update the profile id based on account id "<invalidAccount>" and virtual account id "<invalidVAccount>"
    And I verify the status code as "400"
    And I verify the LCM error code "<lcmErrorCode>" in the response
    And I verify that profile Id update entries are created to event and external logs of common logging service as expected
    And I verify that the response time is under the SLA for "Update VirtualAccount ProfileId" request
    Examples:
      | invalidAccount | invalidVAccount | lcmErrorCode |
      | invalidAccount | ASWFV09809800   | 1011         |

  @Iteration25.3 @GetSchemeToken
  Scenario Outline: Scenario 42 - Verify that retrieve token is unsuccessful for missing authorization
    Given I have the issuer headers as defined
    And I have the scheme token request body as defined in for issuer "<accountType>" and "valid/body/valid_request_body_1.1"
    When I post the details to retrieve scheme tokens endpoint
    Then I verify the status code as "401"
    And I verify that no entries are created to event and external logs of Common logging service
    And I verify that the response time is under the SLA for "Get Scheme Token" request
    Examples:
      | accountType |
      | CARDID      |

  @Iteration25.3 @GetSchemeToken
  Scenario Outline: Scenario 43 - Verify that retrieve token is unsuccessful based on invalid account information
    Given I create a valid bearer token for Issuer service
    And I have the issuer headers as defined
    And I have the scheme token invalid "<invalidAccount>" request body as defined in for issuer "<accountType>" and "valid/body/valid_request_body_1.1"
    When I post the details to retrieve scheme tokens endpoint
    Then I verify the status code as "400"
    And I verify the LCM error code "<lcmErrorCode>" in the response
    And I verify that get scheme token entries are created to event and external logs of common logging service as expected
    And I verify that the response time is under the SLA for "Get Scheme Token" request
    Examples:
      | accountType | invalidAccount  | lcmErrorCode |
      | CARDID      | 123456789009876 | 1013         |

  @Iteration25.3 @GetSchemeToken
  Scenario Outline: Scenario 44 - Verify that retrieve token is successful based on valid account information
    Given I create a valid bearer token for Issuer service
    And I have the issuer headers as defined
    And I have the scheme token request body as defined in for issuer "<accountType>" and "valid/body/valid_request_body_1.1"
    When I post the details to retrieve scheme tokens endpoint
    Then I verify the status code as "200"
    And I verify scheme token details as expected for issuer
    And I verify that get scheme token entries are created to event and external logs of common logging service as expected
    And I verify that the response time is under the SLA for "Get Scheme Token" request
    Examples:
      | accountType |
      | CARDID      |

  @Iteration25.3 @UpdateIDV
  Scenario Outline: Scenario 45 - Verify that Update IDV Method is successful for the valid Account
    Given I create a valid bearer token for Issuer service
    And I have the issuer headers as defined
    And I have update IDV request body as defined "<validRequestBodies>" for "<accountType>" for "<accountValue>" and "<accountState>"
    When I put the request details to UpdateIDV endpoint
    Then I verify the status code as "<statusCode>"
    And I verify the update IDV response as expected
    And I verify that update IDV entries are created for to event and external logs of common logging service as expected
    And I verify that the response time is under the SLA for "Update IDV" request
    Examples:
      | accountType | accountState | validRequestBodies                      | statusCode | accountValue |
      | PAN         | ACTIVE       | valid/body/valid_request_body_UpdateIDV | 200        | valid        |

  @Iteration25.3 @UpdateIDV
  Scenario Outline: Scenario 46 - Verify that Update IDV Method is unsuccessful for the invalid Account
    Given I create a valid bearer token for Issuer service
    And I have the issuer headers as defined
    And I have update IDV request body as defined "valid/body/valid_request_body_UpdateIDV" for "<accountType>" for "<accountValue>" and "<accountState>"
    When I put the request details to UpdateIDV endpoint
    Then I verify the status code as "<statusCode>"
    And I verify the LCM error code "<lcmErrorCode>" in the response body
    And I verify the update IDV response as expected
    And I verify that update IDV entries are created for to event and external logs of common logging service as expected
    And I verify that the response time is under the SLA for "Update IDV" request
    Examples:
      | accountType | accountState | statusCode | accountValue | lcmErrorCode |
      | PAN         | ACTIVE       | 404        | invalid      | 1012         |

  @Iteration25.3 @UpdateIDV
  Scenario Outline: Scenario 47 - Verify that Update IDV Method is unsuccessful for invalid idv method
    Given I create a valid bearer token for Issuer service
    And I have the issuer headers as defined
    And I have update IDV request body as defined "invalid/body/invalid_request_body_UpdateIDV" for "<accountType>" for "<accountValue>" and "<accountState>"
    When I put the request details to UpdateIDV endpoint
    Then I verify the status code as "<statusCode>"
    And I verify the LCM error code "<lcmErrorCode>" in the response body
    And I verify the update IDV response as expected
    And I verify that update IDV entries are created for to event and external logs of common logging service as expected
    And I verify that the response time is under the SLA for "Update IDV" request
    Examples:
      | accountType | accountState | statusCode | accountValue | lcmErrorCode |
      | PAN         | ACTIVE       | 406        | valid        | 1014         |

  @Iteration25.3 @UpdateIDV
  Scenario Outline: Scenario 48 - Verify that Update IDV Method is unsuccessful for missing authorization
    Given I have the issuer headers as defined
    And I have update IDV request body as defined "valid/body/valid_request_body_UpdateIDV" for "<accountType>" for "<accountValue>" and "<accountState>"
    When I put the request details to UpdateIDV endpoint
    Then I verify the status code as "<statusCode>"
    And I verify that no entries are created to event and external logs of Common logging service
    And I verify that the response time is under the SLA for "Update IDV" request
    Examples:
      | accountType | accountState | statusCode | accountValue |
      | PAN         | ACTIVE       | 401        | valid        |

  @Iteration25.3 @UpdateServicesNeeded
  Scenario Outline: Scenario 49 - Verify that Update Services Needed is successful for the valid Account
    Given I create a valid bearer token for Issuer service
    And I have the issuer headers as defined
    And I have Update Services Needed request body as defined "valid/body/valid_request_body_UpdateServices" for "<accountType>" for "<accountValue>" and "<accountState>" and action is "<action>" the "<service>" service
    When I put the request details to Update Services Needed endpoint
    Then I verify the status code as "<statusCode>"
    And I verify that table entries are as expected after update service for "<action>"
    And I verify that verify service needed entries are created for "<action>" to event and external logs of common logging service as expected
    And I verify that the response time is under the SLA for "Update Services Needed" request
    Examples:
      | accountType | accountState | statusCode | accountValue | action | service |
      | PAN         | ACTIVE       | 200        | valid        | REMOVE | TSP     |
      | PAN         | ACTIVE       | 200        | valid        | ADD    | TSP     |

  @Iteration25.3 @UpdateServicesNeeded
  Scenario Outline: Scenario 50 - Verify that Update Services Needed is unsuccessful for the invalid Account
    Given I create a valid bearer token for Issuer service
    And I have the issuer headers as defined
    And I have Update Services Needed request body as defined "valid/body/valid_request_body_UpdateServices" for "<accountType>" for "<accountValue>" and "<accountState>" and action is "<action>" the "<service>" service
    When I put the request details to Update Services Needed endpoint
    Then I verify the status code as "<statusCode>"
    And I verify the LCM error code "<lcmErrorCode>" in the response body
    And I verify that verify service needed entries are created for "<action>" to event and external logs of common logging service as expected
    And I verify that the response time is under the SLA for "Update Services Needed" request
    Examples:
      | accountType | accountState | statusCode | accountValue | action | service | lcmErrorCode |
      | PAN         | ACTIVE       | 200        | invalid      | ADD    | TSP     | 1011-01      |
      | PAN         | ACTIVE       | 200        | invalid      | REMOVE | TSP     | 1011-01      |

  @Iteration25.3 @UpdateServicesNeeded
  Scenario Outline: Scenario 51 - Verify that Update Services Needed is unsuccessful for the invalid request payload
    Given I create a valid bearer token for Issuer service
    And I have the issuer headers as defined
    And I have Update Services Needed request body as defined "valid/body/valid_request_body_UpdateServices" for "<accountType>" for "<accountValue>" and "<accountState>" and action is "<action>" the "<service>" service
    When I put the request details to Update Services Needed endpoint
    Then I verify the status code as "<statusCode>"
    And I verify the LCM error code "<lcmErrorCode>" in the response body
    And I verify that verify service needed entries are created for "<action>" to event and external logs of common logging service as expected
    And I verify that the response time is under the SLA for "Update Services Needed" request
    Examples:
      | accountType | accountState | statusCode | accountValue | action | service | lcmErrorCode |
      | PAN         | ACTIVE       | 200        | valid        | ADD    | ABU     | 1014         |
      | PAN         | ACTIVE       | 200        | valid        | REMOVE | ABU     | 1014         |
      | PAN         | ACTIVE       | 200        | valid        | ADD    | XYZ     | 1014         |
      | PAN         | ACTIVE       | 200        | valid        | REMOVE | PQR     | 1014         |

  @Iteration25.3 @UpdateServicesNeeded
  Scenario Outline: Scenario 52 - Verify that Update Services Needed is unsuccessful for missing authorization
    Given I have the issuer headers as defined
    And I have Update Services Needed request body as defined "valid/body/valid_request_body_UpdateServices" for "<accountType>" for "<accountValue>" and "<accountState>" and action is "<action>" the "<service>" service
    When I put the request details to Update Services Needed endpoint
    Then I verify the status code as "<statusCode>"
    And I verify that no entries are created to event and external logs of Common logging service
    And I verify that the response time is under the SLA for "Update Services Needed" request
    Examples:
      | accountType | accountState | statusCode | accountValue | action | service |
      | PAN         | ACTIVE       | 401        | valid        | REMOVE | ABU     |

  @Iteration25.3 @RenewAccount
  Scenario Outline: Scenario 53 - Verify that updating expiry date is successful for valid account id
    Given I create a valid bearer token for Issuer service
    And I have the issuer headers as defined
    And I have RENEW request body as defined in "valid/body/valid_request_body_Renew" for "<accountType>" for "<accountValue>" and "<accountState>"
    When I renew the account based on account id
    Then I verify the status code as "<statusCode>"
    And I verify the account info as expected after RenewAccount
    And I verify that Renew Account entries are created to event and external logs of common logging service as expected
    And I verify that the response time is under the SLA for "RenewAccount" request
    Examples:
      | accountType | accountState | statusCode | accountValue |
      | PAN         | ACTIVE       | 200        | valid        |

  @Iteration25.3 @RenewAccount
  Scenario Outline: Scenario 54 - Verify that updating expiry date is unsuccessful for invalid account id
    Given I create a valid bearer token for Issuer service
    And I have the issuer headers as defined
    And I have RENEW request body as defined in "valid/body/valid_request_body_Renew" for "<accountType>" for "<accountValue>" and "<accountState>"
    When I renew the account based on account id
    Then I verify the status code as "<statusCode>"
    And I verify the LCM error code "<lcmErrorCode>" in the response body
    And I verify that Renew Account entries are created to event and external logs of common logging service as expected
    And I verify that the response time is under the SLA for "RenewAccount" request
    Examples:
      | accountType | accountState | statusCode | accountValue | lcmErrorCode |
      | PAN         | ACTIVE       | 404        | invalid      | 1012         |

  @Iteration25.3 @RenewAccount
  Scenario Outline: Scenario 55 - Verify that updating expiry date is is unsuccessful for missing authorization
    Given I have the issuer headers as defined
    And I have RENEW request body as defined in "valid/body/valid_request_body_Renew" for "<accountType>" for "<accountValue>" and "<accountState>"
    When I renew the account based on account id
    Then I verify the status code as "<statusCode>"
    And I verify that no entries are created to event and external logs of Common logging service
    And I verify that the response time is under the SLA for "RenewAccount" request
    Examples:
      | accountType | accountState | statusCode | accountValue |
      | PAN         | ACTIVE       | 401        | valid        |