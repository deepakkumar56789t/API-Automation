@MDESTests
Feature: MDES

  @Iteration25.1
    @ASGreen
  Scenario Outline: Scenario 1 - Verify that the authorize service is approved based on wallet provider recommended decision
    Given I encrypt PAN for MDES when token requester as "<tokenRequester>", "<panSource>", account state as "ACTIVE", account ref type as "CARDID"
    And I have the MDES headers
    When I have authorize service request body as defined and approval based on "approveBy" for "Green" flow with "<tokenType>" and "APPROVED" with "no" null field
    And I post the details to authorize service endpoint
    Then I verify the status code as "200"
    And I verify that the decision is as expected "APPROVED"
    And I verify that table entries are as expected after authorize service for "Green"
    Then I verify that MDES authorize service entries are created to event and external logs of Common logging service
    Examples:
      | tokenType   | tokenRequester      | panSource              |
      | cloud       | Google Pay          | account_on_file        |
      | embedded_se | Google Pay          | account_added_manually |
      | static      | Google Pay          | account_on_file        |
      | cloud       | Apple pay           | account_added_manually |
      | embedded_se | Apple pay           | account_on_file        |
      | static      | Apple pay           | account_added_manually |
      | cloud       | define_at_runtime   | account_on_file        |
      | embedded_se | define_at_runtime   | account_added_manually |
      | static      | define_at_runtime   | account_on_file        |
      | cloud       | fetch_from_database | account_added_manually |
      | embedded_se | fetch_from_database | account_on_file        |
      | static      | fetch_from_database | account_added_manually |

  @Iteration25.1
    @ASGreenByWPRCForAP
  Scenario Outline: Scenario 2 - Verify that the authorize service is approved for apple pay where pan source is account_added_via_application and recommendation reason is non HighRisk
    Given I encrypt PAN for MDES when token requester as "<tokenRequester>", "<panSource>", account state as "ACTIVE", account ref type as "CARDID"
    And I have the MDES headers
    When I have authorize service request body as defined and approval based on "recommendation" for "Green" flow with "<tokenType>" and "APPROVED" with "no" null field
    And I post the details to authorize service endpoint
    Then I verify the status code as "200"
    And I verify that the decision is as expected "APPROVED"
    And I verify that table entries are as expected after authorize service for "Green"
    Then I verify that MDES authorize service entries are created to event and external logs of Common logging service
    Examples:
      | tokenType | tokenRequester | panSource                     |
      | static    | Apple pay      | account_added_via_application |

  @Iteration25.1
    @ASGreenByDSForNAP
  Scenario Outline: Scenario 3 - Verify that the authorize service is approved for non apple pay wallets when device score is 1
    Given I encrypt PAN for MDES when token requester as "<tokenRequester>", "<panSource>", account state as "ACTIVE", account ref type as "CARDID"
    And I have the MDES headers
    When I have authorize service request body as defined and approval based on "DeviceScore" for "Red" flow with "<tokenType>" and "APPROVED" with "no" null field
    And I post the details to authorize service endpoint
    Then I verify the status code as "200"
    And I verify that the decision is as expected "APPROVED"
    And I verify that table entries are as expected after authorize service for "Red"
    Then I verify that MDES authorize service entries are created to event and external logs of Common logging service
    Examples:
      | tokenType   | tokenRequester      | panSource                     |
      | cloud       | Google Pay          | account_on_file               |
      | embedded_se | Google Pay          | account_added_manually        |
      | static      | Google Pay          | account_added_via_application |
      | cloud       | define_at_runtime   | account_on_file               |
      | embedded_se | define_at_runtime   | account_added_manually        |
      | static      | define_at_runtime   | account_added_via_application |
      | cloud       | fetch_from_database | account_on_file               |
      | embedded_se | fetch_from_database | account_added_manually        |
      | static      | fetch_from_database | account_added_via_application |

  @Iteration25.2
    @ASGreenByWPRCForNAP
  Scenario Outline: Scenario 4 - Verify that the authorize service is approved for non apple pay wallets based on recommendation reasons
    Given I encrypt PAN for MDES when token requester as "<tokenRequester>", "<panSource>", account state as "ACTIVE", account ref type as "CARDID"
    And I have the MDES headers
    When I have authorize service request body as defined and approval based on "recommendationReasons" for "Green" flow with "<tokenType>" and "APPROVED" with "no" null field
    And I post the details to authorize service endpoint
    Then I verify the status code as "200"
    And I verify that the decision is as expected "APPROVED"
    And I verify that table entries are as expected after authorize service for "Green"
    Then I verify that MDES authorize service entries are created to event and external logs of Common logging service
    Examples:
      | tokenType | tokenRequester      | panSource                     |
      | static    | Google Pay          | account_added_via_application |
      | cloud     | define_at_runtime   | account_added_via_application |
      | cloud     | fetch_from_database | account_added_via_application |

  @Iteration25.2
    @ASGreenByWPRCForNAP
  Scenario Outline: Scenario 5 - Verify that the authorize service is approved for non apple pay where pan source is account_added_via_application and recommendation reason is HighRisk
    Given I encrypt PAN for MDES when token requester as "<tokenRequester>", "<panSource>", account state as "ACTIVE", account ref type as "CARDID"
    And I have the MDES headers
    When I have authorize service request body as defined and approval based on "recommendation" for "Orange" flow with "<tokenType>" and "APPROVED" with "no" null field
    And I post the details to authorize service endpoint
    Then I verify the status code as "200"
    And I verify that the decision is as expected "APPROVED"
    And I verify that table entries are as expected after authorize service for "Orange"
    Then I verify that MDES authorize service entries are created to event and external logs of Common logging service
    Examples:
      | tokenType   | tokenRequester      | panSource                     |
      | cloud       | Google Pay          | account_added_via_application |
      | embedded_se | Google Pay          | account_added_via_application |
      | static      | Google Pay          | account_added_via_application |
      | cloud       | define_at_runtime   | account_added_via_application |
      | embedded_se | define_at_runtime   | account_added_via_application |
      | static      | define_at_runtime   | account_added_via_application |
      | cloud       | fetch_from_database | account_added_via_application |
      | embedded_se | fetch_from_database | account_added_via_application |
      | static      | fetch_from_database | account_added_via_application |

  @Iteration25.1
    @ASYellow
  Scenario Outline: Scenario 6 - Verify that the authorize service required additional authentication based on wallet provider recommended decision
    Given I encrypt PAN for MDES when token requester as "<tokenRequester>", "<panSource>", account state as "ACTIVE", account ref type as "CARDID"
    And I have the MDES headers
    When I have authorize service request body as defined and approval based on "approveBy" for "Yellow" flow with "<tokenType>" and "REQUIRE_ADDITIONAL_AUTHENTICATION" with "no" null field
    And I post the details to authorize service endpoint
    Then I verify the status code as "200"
    And I verify that the decision is as expected "REQUIRE_ADDITIONAL_AUTHENTICATION"
    And I verify that table entries are as expected after authorize service for "Yellow"
    Then I verify that MDES authorize service entries are created to event and external logs of Common logging service
    Examples:
      | tokenType   | tokenRequester      | panSource              |
      | cloud       | Google Pay          | account_on_file        |
      | embedded_se | Google Pay          | account_added_manually |
      | static      | Google Pay          | account_on_file        |
      | cloud       | Apple pay           | account_added_manually |
      | embedded_se | Apple pay           | account_on_file        |
      | static      | Apple pay           | account_added_manually |
      | cloud       | define_at_runtime   | account_on_file        |
      | embedded_se | define_at_runtime   | account_added_manually |
      | static      | define_at_runtime   | account_on_file        |
      | cloud       | fetch_from_database | account_added_manually |
      | embedded_se | fetch_from_database | account_on_file        |
      | static      | fetch_from_database | account_added_manually |

  @Iteration25.2
    @ASYellowByWPRCForAP
  Scenario Outline: Scenario 7 - Verify that the authorize service required additional authentication for apple pay where pan source is account_added_via_application and recommendation reason is HIGH_RISK
    Given I encrypt PAN for MDES when token requester as "<tokenRequester>", "<panSource>", account state as "ACTIVE", account ref type as "CARDID"
    And I have the MDES headers
    When I have authorize service request body as defined and approval based on "recommendation" for "Orange" flow with "<tokenType>" and "APPROVED" with "no" null field
    And I post the details to authorize service endpoint
    Then I verify the status code as "200"
    And I verify that the given pan is required additional authentication for authorize service as expected
    And I verify that table entries are as expected after authorize service for "Orange"
    Then I verify that MDES authorize service entries are created to event and external logs of Common logging service
    Examples:
      | tokenType | tokenRequester | panSource                     |
      | static    | Apple pay      | account_added_via_application |

  @Iteration25.2
    @ASYellowByRSKWLT
  Scenario Outline: Scenario 8 - Verify that the authorize service required additional authentication where device score and recommended decision is Null
    Given I encrypt PAN for MDES when token requester as "<tokenRequester>", "<panSource>", account state as "ACTIVE", account ref type as "CARDID"
    And I have the MDES headers
    When I have authorize service request body as defined and approval based on "walletRisks" for "yellow" flow with "<tokenType>" and "APPROVED" with "walletRisks" null field
    And I post the details to authorize service endpoint
    Then I verify the status code as "200"
    And I verify that the given pan is required additional authentication for authorize service as expected
    And I verify that table entries are as expected after authorize service for "yellow"
    Then I verify that MDES authorize service entries are created to event and external logs of Common logging service
    Examples:
      | tokenType   | tokenRequester | panSource              |
      | embedded_se | Apple pay      | account_on_file        |
      | embedded_se | Apple pay      | account_added_manually |
      | static      | Google Pay     | account_on_file        |
      | static      | Google Pay     | account_added_manually |

  @Iteration25.2
    @ASYellowByRSKWLT
  Scenario Outline: Scenario 9 - Verify that the authorize service required additional authentication where wallet Provider Decisioning Info is Null
    Given I encrypt PAN for MDES when token requester as "<tokenRequester>", "<panSource>", account state as "ACTIVE", account ref type as "CARDID"
    And I have the MDES headers
    When I have authorize service request body as defined and approval based on "walletProviderDecisioningInfo" for "yellow" flow with "<tokenType>" and "APPROVED" with "walletProviderDecisioningInfo" null field
    And I post the details to authorize service endpoint
    Then I verify the status code as "200"
    And I verify that the given pan is required additional authentication for authorize service as expected
    And I verify that table entries are as expected after authorize service for "yellow"
    Then I verify that MDES authorize service entries are created to event and external logs of Common logging service
    Examples:
      | tokenType   | tokenRequester | panSource              |
      | embedded_se | Apple pay      | account_on_file        |
      | embedded_se | Apple pay      | account_added_manually |
      | static      | Google Pay     | account_on_file        |
      | static      | Google Pay     | account_added_manually |

  @Iteration25.2
    @ASRed
  Scenario Outline: Scenario 10 - Verify that the authorize service is declined based on wallet provider recommended decision
    Given I encrypt PAN for MDES when token requester as "<tokenRequester>", "<panSource>", account state as "ACTIVE", account ref type as "CARDID"
    And I have the MDES headers
    When I have authorize service request body as defined and approval based on "approveBy" for "Red" flow with "<tokenType>" and "DECLINED" with "no" null field
    And I post the details to authorize service endpoint
    Then I verify the status code as "200"
    And I verify that the given pan is declined for authorize service as expected
    And I verify that table entries are as expected after authorize service for "Red"
    Then I verify that MDES authorize service entries are created to event and external logs of Common logging service
    Examples:
      | tokenType   | tokenRequester      | panSource              |
      | cloud       | Google Pay          | account_on_file        |
      | embedded_se | Google Pay          | account_added_manually |
      | static      | Google Pay          | account_on_file        |
      | cloud       | Apple pay           | account_added_manually |
      | embedded_se | Apple pay           | account_on_file        |
      | static      | Apple pay           | account_added_manually |
      | cloud       | define_at_runtime   | account_on_file        |
      | embedded_se | define_at_runtime   | account_added_manually |
      | static      | define_at_runtime   | account_on_file        |
      | cloud       | fetch_from_database | account_added_manually |
      | embedded_se | fetch_from_database | account_on_file        |
      | static      | fetch_from_database | account_added_manually |

  @Iteration25.1
    @ASRedByDSForAP
  Scenario Outline: Scenario 11 - Verify that the authorize service is declined for apple pay wallet when device score is 1
    Given I encrypt PAN for MDES when token requester as "<tokenRequester>", "<panSource>", account state as "ACTIVE", account ref type as "CARDID"
    And I have the MDES headers
    When I have authorize service request body as defined and approval based on "DeviceScore" for "Red" flow with "<tokenType>" and "APPROVED" with "no" null field
    And I post the details to authorize service endpoint
    Then I verify the status code as "200"
    And I verify that the given pan is declined for authorize service as expected
    And I verify that table entries are as expected after authorize service for "<Red>"
    Then I verify that MDES authorize service entries are created to event and external logs of Common logging service
    Examples:
      | tokenType   | tokenRequester | panSource                     |
      | embedded_se | Apple pay      | account_on_file               |
      | embedded_se | Apple pay      | account_added_manually        |
      | embedded_se | Apple pay      | account_added_via_application |

  @Iteration25.2
    @ASRedByWPRCForAP
  Scenario Outline: Scenario 12 - Verify that the authorize service is declined for apple pay wallets based on recommendation reasons
    Given I encrypt PAN for MDES when token requester as "<tokenRequester>", "<panSource>", account state as "ACTIVE", account ref type as "CARDID"
    And I have the MDES headers
    When I have authorize service request body as defined and approval based on "recommendationReasons" for "Red" flow with "<tokenType>" and "APPROVED" with "no" null field
    And I post the details to authorize service endpoint
    Then I verify the status code as "200"
    And I verify that the given pan is declined for authorize service as expected
    And I verify that table entries are as expected after authorize service for "Red"
    Then I verify that MDES authorize service entries are created to event and external logs of Common logging service
    Examples:
      | tokenType   | tokenRequester | panSource                     |
      | embedded_se | Apple pay      | account_on_file               |
      | embedded_se | Apple pay      | account_added_manually        |
      | embedded_se | Apple pay      | account_added_via_application |

  @Iteration26.1 @MDES_E2E_ASGreen
    @GetToken
    @UpdateAccountState
    @UpdateVirtualAccountState
#    @GetAccountAndVirtualAccount
    @UpdateProfile
  Scenario Outline: Scenario 13 - Verify that the token provisioning is processed successfully while authorize service is approved based on wallet provider recommended decision
    Given Pre-requisite: To validate authorize service with the given details token requester as "<tokenRequester>", "<panSource>", account state as "ACTIVE", account ref type as "CARDID" and approval based on "approveBy" for "Green" flow with "<tokenType>" and "APPROVED" with "no" null field
    When I Verify that Notify Service is activated as expected for the given details "<tokenType>" and "APPROVED"
#   Then I Verify that Notify Token is Updated as "<status>" for the given details
    Examples:
      | tokenType   | tokenRequester      | panSource              |
      | cloud       | Google Pay          | account_on_file        |
      | embedded_se | Google Pay          | account_added_manually |
      | static      | Google Pay          | account_on_file        |
      | cloud       | Apple pay           | account_added_manually |
      | embedded_se | Apple pay           | account_on_file        |
      | static      | Apple pay           | account_added_manually |
      | cloud       | define_at_runtime   | account_on_file        |
      | embedded_se | define_at_runtime   | account_added_manually |
      | static      | define_at_runtime   | account_on_file        |
      | cloud       | fetch_from_database | account_added_manually |
      | embedded_se | fetch_from_database | account_on_file        |
      | static      | fetch_from_database | account_added_manually |

  @Iteration26.1 @MDES_E2E_ASYellow
    @GetToken
    @UpdateAccountState
    @UpdateVirtualAccountState
#    @GetAccountAndVirtualAccount
    @UpdateProfile
  Scenario Outline: Scenario 14 - Verify that the token provisioning is processed successfully while authorize service required additional authentication based on wallet provider recommended decision
    Given Pre-requisite: To validate authorize service with the given details token requester as "<tokenRequester>", "<panSource>", account state as "ACTIVE", account ref type as "CARDID" and approval based on "approveBy" for "Yellow" flow with "<tokenType>" and "REQUIRE_ADDITIONAL_AUTHENTICATION" with "no" null field
    When I Verify that Request Activation Methods is successfull for the given details
    And I Verify that Deliver Activation code is successfull for the given details
    Then I Verify that Notify Service is activated as expected for the given details "<tokenType>" and "REQUIRE_ADDITIONAL_AUTHENTICATION"
#   Then I Verify that Notify Token is Updated as "<status>" for the given details
    Examples:
      | tokenType   | tokenRequester      | panSource              |
      | cloud       | Google Pay          | account_on_file        |
      | embedded_se | Google Pay          | account_added_manually |
      | static      | Google Pay          | account_on_file        |
      | cloud       | Apple pay           | account_added_manually |
      | embedded_se | Apple pay           | account_on_file        |
      | static      | Apple pay           | account_added_manually |
      | cloud       | define_at_runtime   | account_on_file        |
      | embedded_se | define_at_runtime   | account_added_manually |
      | static      | define_at_runtime   | account_on_file        |
      | cloud       | fetch_from_database | account_added_manually |
      | embedded_se | fetch_from_database | account_on_file        |
      | static      | fetch_from_database | account_added_manually |

  @Iteration26.1 @MDES_E2E_ASGreenByWPRCForAP
    @GetToken
    @InAppPayload
    @UpdateAccountState
    @UpdateVirtualAccountState
#    @GetAccountAndVirtualAccount
    @UpdateProfile
  Scenario Outline: Scenario 15 - Verify that the token provisioning is processed successfully for apple pay where pan source is ACCOUNT_ADDED_VIA_APPLICATION and recommendation reason is non HighRisk
    Given Pre-requisite: To validate authorize service with the given details token requester as "<tokenRequester>", "<panSource>", account state as "ACTIVE", account ref type as "CARDID" and approval based on "recommendation" for "Green" flow with "<tokenType>" and "APPROVED" with "no" null field
    When I Verify that Notify Service is activated as expected for the given details "<tokenType>" and "APPROVED"
#   Then I Verify that Notify Token is Updated as "<status>" for the given details
    Examples:
      | tokenType   | tokenRequester | panSource                     |
      | embedded_se | Apple pay      | account_added_via_application |

  @Iteration26.1 @MDES_E2E_ASYellowByWPRCForAP
    @GetToken
    @InAppPayload
    @UpdateAccountState
    @UpdateVirtualAccountState
#    @GetAccountAndVirtualAccount
    @UpdateProfile
  Scenario Outline: Scenario 16 - Verify that the token provisioning is processed successfully for apple pay where pan source is ACCOUNT_ADDED_VIA_APPLICATION and recommendation reason is HighRisk
    Given Pre-requisite: To validate authorize service with the given details token requester as "<tokenRequester>", "<panSource>", account state as "ACTIVE", account ref type as "CARDID" and approval based on "recommendation" for "Orange" flow with "<tokenType>" and "APPROVED" with "no" null field
    When I Verify that Request Activation Methods is successfull for the given details
    And I Verify that Deliver Activation code is successfull for the given details
    Then I Verify that Notify Service is activated as expected for the given details "Orange" and "APPROVED"
#    And I Verify that Notify Token is Updated as "<status>" for the given details
    Examples:
      | tokenType   | tokenRequester | panSource                     | status    |
      | embedded_se | Apple pay      | account_added_via_application | SUSPENDED |

  @Iteration26.1 @MDES_E2E_ASYellowByRSKWLT
    @GetToken
    @InAppPayload
    @UpdateAccountState
    @UpdateVirtualAccountState
#    @GetAccountAndVirtualAccount
    @UpdateProfile
  Scenario Outline: Scenario 17 - Verify that the token provisioning is processed successfully where device score and recommended decision is Null
    Given Pre-requisite: To validate authorize service with the given details token requester as "<tokenRequester>", "<panSource>", account state as "ACTIVE", account ref type as "CARDID" and approval based on "walletRisks" for "yellow" flow with "<tokenType>" and "APPROVED" with "no" null field
    When I Verify that Request Activation Methods is successfull for the given details
    And I Verify that Deliver Activation code is successfull for the given details
    Then I Verify that Notify Service is activated as expected for the given details "<tokenType>" and "APPROVED"
    Examples:
      | tokenType   | tokenRequester | panSource              |
      | embedded_se | Apple pay      | account_on_file        |
      | embedded_se | Apple pay      | account_added_manually |
      | static      | Google Pay     | account_on_file        |
      | static      | Google Pay     | account_added_manually |

  @Iteration26.1
    @MDES_E2E_ASFailedNonActiveTestCard
  Scenario Outline: Scenario 18 - Verify that the token provisioning is declined for pan with other than active state
    Given I encrypt PAN for MDES when token requester as "<tokenRequester>", "<panSource>", account state as "DELETED", account ref type as "CARDID"
    And I have the MDES headers
    When I have authorize service request body as defined and approval based on "approveBy" for "Green" flow with "<tokenType>" and "APPROVED" with "no" null field
    And I post the details to authorize service endpoint
    Then I verify the status code as "200"
    And I verify that token provisioning is declined and error code is as expected "invalid_field_value"
#    Then I verify that MDES authorize service entries are created to event and external logs of Common logging service
    Examples:
      | tokenType   | tokenRequester      | panSource              |
      | cloud       | Google Pay          | account_on_file        |
      | embedded_se | Google Pay          | account_added_manually |
      | static      | Google Pay          | account_on_file        |
      | cloud       | Apple pay           | account_added_manually |
      | embedded_se | Apple pay           | account_on_file        |
      | static      | Apple pay           | account_added_manually |
      | cloud       | define_at_runtime   | account_on_file        |
      | embedded_se | define_at_runtime   | account_added_manually |
      | static      | define_at_runtime   | account_on_file        |
      | cloud       | fetch_from_database | account_added_manually |
      | embedded_se | fetch_from_database | account_on_file        |
      | static      | fetch_from_database | account_added_manually |

  @Iteration26.1
    @ASFailWhenLimitExceed
  Scenario Outline: Scenario 19 - Verify that the token provisioning is not processed when the virtual card limit is exceeded
    Given I encrypt PAN for MDES when token requester as "<tokenRequester>", "<panSource>", account state as "ACTIVE", account ref type as "CARDID"
    And I have the MDES headers
    When I have authorize service request body as defined and approval based on "approveBy" for "Yellow" flow with "<tokenType>" and "REQUIRE_ADDITIONAL_AUTHENTICATION" with "no" null field
    And I verify that the virtual card limit of given issuer bin range and token requester for MDES
    And I post the details to authorize service endpoint exceeding virtual card limit
    Then I verify the status code as "200"
    And I verify that token provisioning is declined and error code is as expected "invalid_field_value"
#   Then I verify that MDES authorize service entries are created to event and external logs of Common logging service
    Examples:
      | tokenType   | tokenRequester    | panSource              |
      | static      | define_at_runtime | account_on_file        |
      | embedded_se | define_at_runtime | account_added_manually |

  @Iteration26.1
    @IncorrectActivationMethodType
  Scenario Outline:Scenario 20 - Verify that the token provisioning is not processed for google pay and apple pay wallets due to incorrect authentication method type
    Given Pre-requisite: To validate authorize service with the given details token requester as "<tokenRequester>", "<panSource>", account state as "ACTIVE", account ref type as "CARDID" and approval based on "approveBy" for "Yellow" flow with "<tokenType>" and "REQUIRE_ADDITIONAL_AUTHENTICATION" with "no" null field
    When I Verify that Request Activation Methods is successfull for the given details
    And I Verify that Deliver Activation code is unsuccessfull for the given details with incorrect activation method "<activationMethodType>" and "<activationMethodValue>"
    Then I verify that token provisioning is declined and error code is as expected "invalid_field_value"
#    And I verify that MDES Deliver Activation Code entries are created to event and external logs of Common logging service
    Examples:
      | tokenType | tokenRequester | panSource       | activationMethodType | activationMethodValue |
      | cloud     | Google Pay     | account_on_file | define_at_runtime    | define_at_runtime     |
      | cloud     | Google Pay     | account_on_file | TEXT                 | 1111111111            |