@VTSTests
Feature: VTS

  @Iteration22.4 @CheckEligibility
  Scenario Outline: Scenario 01 - Verify that the given pan is eligible for visa token provisioning process
    Given I encrypt PAN for check eligibility when token requester as "<tokenRequester>", account state as "ACTIVE", profile id as "any", account ref type as "CARDID" with "no" null field and idvMethod as "any"
    And I have the default VTS headers
    When I have the check eligibility request body as defined and pan source as "<panSource>"
    And I post the details to check eligibility endpoint
    Then I verify the status code as "200"
    And I verify that the given pan is eligible for visa token provisioning as expected
    And I verify that table entries are as expected after check eligibility for "<tokenRequester>" tokenRequester
    And I verify that entries are created for check eligibility request in event and external log tables of Common logging service
    And I verify that the response time is under the SLA for check eligibility endpoint
    Examples:
      | panSource          | tokenRequester      |
      | on_file            | Google Pay          |
      | key_entered        | Google Pay          |
      | mobile_banking_app | Google Pay          |
      | on_file            | Apple pay           |
      | key_entered        | Apple pay           |
      | mobile_banking_app | Apple pay           |
      | on_file            | fetch_from_database |
      | key_entered        | fetch_from_database |
      | mobile_banking_app | fetch_from_database |
      | on_file            | define_at_runtime   |
      | key_entered        | define_at_runtime   |
      | mobile_banking_app | define_at_runtime   |

  @Iteration22.4 @APGreenByRskScore
  @VerifyThresholdRangeGreen
  Scenario Outline: Scenario 02 - Verify that the token provisioning is approved based on risk assessment score
    Given I encrypt PAN for approve provisioning when token requester as "<tokenRequester>", account state as "ACTIVE", profile id as "any", account ref type as "CARDID", idvMethod as "any" and approval based on "riskAssessmentScore" for "<flowType>" flow with "no" null field
    And I have the default VTS headers
    When I have the approve provisioning request body as defined "<panSource>", "<tokenType>", "null" and "any" 
    And I post the details to approve provisioning endpoint
    Then I verify the status code as "200"
    And I verify that the given pan is approved for visa token provisioning as expected with action code as "00"
    And I verify that table entries are as expected after approve provisioning for "<tokenType>" and "<flowType>"
    And I verify that entries are created for approve provisioning request in event and external log tables of Common logging service
    And I verify that the response time is under the SLA for approve provisioning endpoint
    Examples:
      | panSource          | tokenType      | tokenRequester      | flowType |
      | on_file            | hce            | Google Pay          | green    |
      | key_entered        | hce            | Google Pay          | green    |
      | mobile_banking_app | hce            | Google Pay          | green    |
      | on_file            | secure_element | Apple pay           | green    |
      | key_entered        | secure_element | Apple pay           | green    |
      | mobile_banking_app | secure_element | Apple pay           | green    |
      | on_file            | card_on_file   | fetch_from_database | green    |
      | key_entered        | card_on_file   | fetch_from_database | green    |
      | mobile_banking_app | card_on_file   | fetch_from_database | green    |
      | on_file            | ecommerce      | fetch_from_database | green    |
      | key_entered        | ecommerce      | fetch_from_database | green    |
      | mobile_banking_app | ecommerce      | fetch_from_database | green    |
      | on_file            | card_on_file   | define_at_runtime   | green    |
      | key_entered        | card_on_file   | define_at_runtime   | green    |
      | mobile_banking_app | card_on_file   | define_at_runtime   | green    |
      | on_file            | ecommerce      | define_at_runtime   | green    |
      | key_entered        | ecommerce      | define_at_runtime   | green    |
      | mobile_banking_app | ecommerce      | define_at_runtime   | green    |

  @Iteration22.4 @APGreenByWPRCForAP
  Scenario Outline: Scenario 03 - Verify that the token provisioning is approved for apple pay where pan source is mobile_banking_app and  walletProviderReasonCodes is non 0G
    Given I encrypt PAN for approve provisioning when token requester as "<tokenRequester>", account state as "ACTIVE", profile id as "any", account ref type as "CARDID", idvMethod as "any" and approval based on "walletProviderReasonCodes" for "<flowType>" flow with "no" null field
    And I have the default VTS headers
    When I have the approve provisioning request body as defined "<panSource>", "<tokenType>", "null" and "any" 
    And I post the details to approve provisioning endpoint
    Then I verify the status code as "200"
    And I verify that the given pan is approved for visa token provisioning as expected with action code as "00"
    And I verify that table entries are as expected after approve provisioning for "<tokenType>" and "<flowType>"
    And I verify that entries are created for approve provisioning request in event and external log tables of Common logging service
    And I verify that the response time is under the SLA for approve provisioning endpoint
    Examples:
      | panSource          | tokenType      | tokenRequester | flowType |
      | mobile_banking_app | secure_element | Apple pay      | green    |

  @Iteration22.4 @APGreenByDSForNAP
  Scenario Outline: Scenario 04 - Verify that the token provisioning is approved for non apple pay wallets when wallet provider device score is 1
    Given I encrypt PAN for approve provisioning when token requester as "<tokenRequester>", account state as "ACTIVE", profile id as "any", account ref type as "CARDID", idvMethod as "any" and approval based on "walletProviderDeviceScore" for "<flowType>" flow with "no" null field
    And I have the default VTS headers
    When I have the approve provisioning request body as defined "<panSource>", "<tokenType>", "null" and "any" 
    And I post the details to approve provisioning endpoint
    Then I verify the status code as "200"
    And I verify that the given pan is approved for visa token provisioning as expected with action code as "00"
    And I verify that table entries are as expected after approve provisioning for "<tokenType>" and "green"
    And I verify that entries are created for approve provisioning request in event and external log tables of Common logging service
    And I verify that the response time is under the SLA for approve provisioning endpoint
    Examples:
      | panSource          | tokenType    | tokenRequester      | flowType |
      | mobile_banking_app | hce          | Google Pay          | red      |
      | mobile_banking_app | card_on_file | fetch_from_database | red      |
      | mobile_banking_app | ecommerce    | fetch_from_database | red      |
      | mobile_banking_app | card_on_file | define_at_runtime   | red      |
      | mobile_banking_app | ecommerce    | define_at_runtime   | red      |

  @Iteration22.4 @APGreenByWPRCForNAP
  Scenario Outline: Scenario 05 - Verify that the token provisioning is approved for non apple pay wallets based on wallet provider reason codes
    Given I encrypt PAN for approve provisioning when token requester as "<tokenRequester>", account state as "ACTIVE", profile id as "any", account ref type as "CARDID", idvMethod as "any" and approval based on "walletProviderReasonCodes" for "<flowType>" flow with "no" null field
    And I have the default VTS headers
    When I have the approve provisioning request body as defined "<panSource>", "<tokenType>", "null" and "any" 
    And I post the details to approve provisioning endpoint
    Then I verify the status code as "200"
    And I verify that the given pan is approved for visa token provisioning as expected with action code as "00"
    And I verify that table entries are as expected after approve provisioning for "<tokenType>" and "green"
    And I verify that entries are created for approve provisioning request in event and external log tables of Common logging service
    And I verify that the response time is under the SLA for approve provisioning endpoint
    Examples:
      | panSource          | tokenType      | tokenRequester      | flowType |
      | mobile_banking_app | hce            | Google Pay          | red      |
      | mobile_banking_app | card_on_file   | fetch_from_database | red      |
      | mobile_banking_app | ecommerce      | fetch_from_database | red      |
      | mobile_banking_app | card_on_file   | define_at_runtime   | red      |
      | mobile_banking_app | ecommerce      | define_at_runtime   | red      |

  @Iteration22.4 @APOrangeByWPRCForAP
  Scenario Outline: Scenario 06 - Verify that the token provisioning is required additional authentication for apple pay where pan source is mobile_banking_app and walletProviderReasonCodes is 0G
    Given I encrypt PAN for approve provisioning when token requester as "<tokenRequester>", account state as "ACTIVE", profile id as "any", account ref type as "CARDID", idvMethod as "any" and approval based on "walletProviderReasonCodes" for "<flowType>" flow with "no" null field
    And I have the default VTS headers
    When I have the approve provisioning request body as defined "<panSource>", "<tokenType>", "null" and "any" 
    And I post the details to approve provisioning endpoint
    Then I verify the status code as "200"
    And I verify that the given pan is required additional authentication for visa token provisioning as given "<flowType>"
    And I verify that table entries are as expected after approve provisioning for "<tokenType>" and "<flowType>"
    And I verify that entries are created for approve provisioning request in event and external log tables of Common logging service
    And I verify that the response time is under the SLA for approve provisioning endpoint
    Examples:
      | panSource          | tokenType      | tokenRequester | flowType |
      | mobile_banking_app | secure_element | Apple pay      | Orange   |

  @Iteration22.4 @APYellowByRskScore
  Scenario Outline: Scenario 07 - Verify that the token provisioning is required additional authentication based on risk assessment score
    Given I encrypt PAN for approve provisioning when token requester as "<tokenRequester>", account state as "ACTIVE", profile id as "any", account ref type as "CARDID", idvMethod as "any" and approval based on "riskAssessmentScore" for "<flowType>" flow with "no" null field
    And I have the default VTS headers
    When I have the approve provisioning request body as defined "<panSource>", "<tokenType>", "null" and "any" 
    And I post the details to approve provisioning endpoint
    Then I verify the status code as "200"
    And I verify that the given pan is required additional authentication for visa token provisioning as given "<flowType>"
    And I verify that table entries are as expected after approve provisioning for "<tokenType>" and "<flowType>"
    And I verify that entries are created for approve provisioning request in event and external log tables of Common logging service
    And I verify that the response time is under the SLA for approve provisioning endpoint
    Examples:
      | panSource   | tokenType      | tokenRequester | flowType |
      | on_file     | hce            | Google Pay     | yellow   |
      | key_entered | hce            | Google Pay     | yellow   |
      | on_file     | secure_element | Apple pay      | yellow   |
      | key_entered | secure_element | Apple pay      | yellow   |

  @Iteration22.4 @APYellowByRskWltInfo
  Scenario Outline: Scenario 08 - Verify that the token provisioning is required additional authentication when risk and wallet info are null
    Given I encrypt PAN for approve provisioning when token requester as "<tokenRequester>", account state as "ACTIVE", profile id as "any", account ref type as "CARDID", idvMethod as "any" and approval based on "walletRisks" for "<flowType>" flow with "walletRisks" null field
    And I have the default VTS headers
    When I have the approve provisioning request body as defined "<panSource>", "<tokenType>", "null" and "any" 
    And I post the details to approve provisioning endpoint
    Then I verify the status code as "200"
    And I verify that the given pan is required additional authentication for visa token provisioning as given "<flowType>"
    And I verify that table entries are as expected after approve provisioning for "<tokenType>" and "<flowType>"
    And I verify that entries are created for approve provisioning request in event and external log tables of Common logging service
    And I verify that the response time is under the SLA for approve provisioning endpoint
    Examples:
      | panSource   | tokenType      | tokenRequester | flowType |
      | on_file     | hce            | Google Pay     | yellow   |
      | key_entered | hce            | Google Pay     | yellow   |
      | on_file     | secure_element | Apple pay      | yellow   |
      | key_entered | secure_element | Apple pay      | yellow   |

  @Iteration22.4 @APRedByRskScore
  @VerifyThresholdRangeRed
  Scenario Outline: Scenario 09 - Verify that the token provisioning is declined based on risk assessment score
    Given I encrypt PAN for approve provisioning when token requester as "<tokenRequester>", account state as "ACTIVE", profile id as "any", account ref type as "CARDID", idvMethod as "any" and approval based on "riskAssessmentScore" for "<flowType>" flow with "no" null field
    And I have the default VTS headers
    When I have the approve provisioning request body as defined "<panSource>", "<tokenType>", "null" and "any" 
    And I post the details to approve provisioning endpoint
    Then I verify the status code as "200"
    And I verify that the given pan is declined for visa token provisioning as expected
    And I verify that table entries are as expected after approve provisioning for "<tokenType>" and "<flowType>"
    And I verify that entries are created for approve provisioning request in event and external log tables of Common logging service
    And I verify that the response time is under the SLA for approve provisioning endpoint
    Examples:
      | panSource   | tokenType      | tokenRequester | flowType |
      | on_file     | hce            | Google Pay     | red      |
      | key_entered | hce            | Google Pay     | red      |
      | on_file     | secure_element | Apple pay      | red      |
      | key_entered | secure_element | Apple pay      | red      |

  @Iteration22.4 @APRedByDSForAP
  Scenario Outline: Scenario 10 - Verify that the token provisioning is declined for apple pay wallet when wallet provider device score is 1
    Given I encrypt PAN for approve provisioning when token requester as "<tokenRequester>", account state as "ACTIVE", profile id as "any", account ref type as "CARDID", idvMethod as "any" and approval based on "walletProviderDeviceScore" for "<flowType>" flow with "no" null field
    And I have the default VTS headers
    When I have the approve provisioning request body as defined "<panSource>", "<tokenType>", "null" and "any" 
    And I post the details to approve provisioning endpoint
    Then I verify the status code as "200"
    And I verify that the given pan is declined for visa token provisioning as expected
    And I verify that table entries are as expected after approve provisioning for "<tokenType>" and "<flowType>"
    And I verify that entries are created for approve provisioning request in event and external log tables of Common logging service
    And I verify that the response time is under the SLA for approve provisioning endpoint
    Examples:
      | panSource          | tokenType      | tokenRequester | flowType |
      | on_file            | secure_element | Apple pay      | red      |
      | key_entered        | secure_element | Apple pay      | red      |
      | mobile_banking_app | secure_element | Apple pay      | red      |

  @Iteration22.4 @APRedByWPRCForAP
  Scenario Outline: Scenario 11 - Verify that the token provisioning is declined for apple pay wallet based on wallet provider reason codes
    Given I encrypt PAN for approve provisioning when token requester as "<tokenRequester>", account state as "ACTIVE", profile id as "any", account ref type as "CARDID", idvMethod as "any" and approval based on "walletProviderReasonCodes" for "<flowType>" flow with "no" null field
    And I have the default VTS headers
    When I have the approve provisioning request body as defined "<panSource>", "<tokenType>", "null" and "any" 
    And I post the details to approve provisioning endpoint
    Then I verify the status code as "200"
    And I verify that the given pan is declined for visa token provisioning as expected
    And I verify that table entries are as expected after approve provisioning for "<tokenType>" and "<flowType>"
    And I verify that entries are created for approve provisioning request in event and external log tables of Common logging service
    And I verify that the response time is under the SLA for approve provisioning endpoint
    Examples:
      | panSource          | tokenType      | tokenRequester | flowType |
      | on_file            | secure_element | Apple pay      | red      |
      | key_entered        | secure_element | Apple pay      | red      |
      | mobile_banking_app | secure_element | Apple pay      | red      |

#  @Iteration22.4 @SkipTestCards @APRedInvalidCVVAttemptsForAP
#  Scenario Outline: Scenario 12 - Verify that the token provisioning is declined for apple pay when invalid cvv attempts exceed 5 times
#    Given I encrypt PAN for approve provisioning when token requester as "<tokenRequester>", account state as "ACTIVE", profile id as "any", account ref type as "CARDID", idvMethod as "any" and approval based on "riskAssessmentScore" for "<flowType>" flow with "no" null field
#    And I have the default VTS headers
#    When I have the approve provisioning request body as defined "<panSource>", "<tokenType>", "null" and "any" 
#    And I post the details to approve provisioning endpoint for more than 5 times
#    Then I verify the status code as "200"
#    And I verify that the given pan is declined for visa token provisioning as expected
#    And I verify that table entries are as expected after approve provisioning for "<tokenType>" and "<flowType>"
#    And I verify that entries are created for approve provisioning request in event and external log tables of Common logging service
#    And I verify that the response time is under the SLA for approve provisioning endpoint
#    Examples:
#      | panSource   | tokenType      | tokenRequester | flowType |
#      | on_file     | secure_element | Apple pay      | yellow   |

  @Iteration22.4 @APFailWhenLimitExceed
  Scenario Outline: Scenario 13 - Verify that the token provisioning is not processed when the virtual card limit is exceeded
    Given I encrypt PAN for approve provisioning when token requester as "<tokenRequester>", account state as "ACTIVE", profile id as "any", account ref type as "CARDID", idvMethod as "any" and approval based on "riskAssessmentScore" for "<flowType>" flow with "no" null field
    And I have the default VTS headers
    When I have the approve provisioning request body as defined "<panSource>", "<tokenType>", "null" and "any" 
    And I verify that the virtual card limit of given issuer bin range and token requester
    And I post the details to approve provisioning endpoint exceeding virtual card limit
    Then I verify the status code as "200"
    And I verify the error code "<errorCode>" in the response
    And I verify that entries are created for approve provisioning request in event and external log tables of Common logging service
    And I verify that the response time is under the SLA for approve provisioning endpoint
    Examples:
      | panSource          | tokenType    | tokenRequester      | flowType | errorCode |
      | on_file            | card_on_file | fetch_from_database | yellow   | ISE40001  |
      | key_entered        | card_on_file | fetch_from_database | yellow   | ISE40001  |
      | mobile_banking_app | card_on_file | fetch_from_database | yellow   | ISE40001  |
      | on_file            | ecommerce    | fetch_from_database | yellow   | ISE40001  |
      | key_entered        | ecommerce    | fetch_from_database | yellow   | ISE40001  |
      | mobile_banking_app | ecommerce    | fetch_from_database | yellow   | ISE40001  |

#  @Iteration23.4 @createAccountOrUpdateAccountState @CEFailsNonActiveForNonAPGP
#  Scenario Outline: Scenario 14 - Verify that the check eligibility fails for pan with other than active state when token requester is other than google pay and apple pay
#    Given I encrypt PAN for check eligibility when token requester as "<tokenRequester>", account state as "<accountState>", profile id as "any", account ref type as "CARDID" with "no" null field and idvMethod as "any"
#    And I have the default VTS headers
#    When I have the check eligibility request body as defined in "<validRequestBodies>" and pan source as "<panSource>"
#    And I post the details to check eligibility endpoint
#    Then I verify the status code as "200"
#    And I verify the error code "<errorCode>" in the response
#    And I verify that entries are created for approve provisioning request in event and external log tables of Common logging service
#    And I verify that the response time is under the SLA for approve provisioning endpoint
#    Examples:
#      | panSource          | tokenRequester      | accountState | errorCode |
#      | on_file            | fetch_from_database | DELETED      | ISE40001  |
#      | key_entered        | fetch_from_database | DELETED      | ISE40001  |
#      | mobile_banking_app | fetch_from_database | DELETED      | ISE40001  |
#      | on_file            | fetch_from_database | DELETED      | ISE40001  |
#      | key_entered        | fetch_from_database | DELETED      | ISE40001  |
#      | mobile_banking_app | fetch_from_database | DELETED      | ISE40001  |
#      | on_file            | fetch_from_database | SUSPENDED    | ISE40001  |
#      | key_entered        | fetch_from_database | SUSPENDED    | ISE40001  |
#      | mobile_banking_app | fetch_from_database | SUSPENDED    | ISE40001  |
#      | on_file            | fetch_from_database | SUSPENDED    | ISE40001  |
#      | key_entered        | fetch_from_database | SUSPENDED    | ISE40001  |
#      | mobile_banking_app | fetch_from_database | SUSPENDED    | ISE40001  |
#
#  @Iteration23.4 @createAccountOrUpdateAccountState @APRedNonActiveForAPGP
#  Scenario Outline: Scenario 15 - Verify that the token provisioning is declined for pan with other than active state when token requester is either google pay or apple pay
#    Given Pre-requisite: I verify check eligibility for given PAN when token requester as "<tokenRequester>", account state as "<accountState>", profile id as "any", account ref type as "CARDID", idvMethod as "any" and pan source as "<panSource>" with "no" null field
#    Given I encrypt PAN for approve provisioning when token requester as "<tokenRequester>", account state as "<accountState>", profile id as "any", account ref type as "CARDID", idvMethod as "any" and approval based on "riskAssessmentScore" for "<flowType>" flow with "no" null field
#    And I have the default VTS headers
#    When I have the approve provisioning request body as defined "<panSource>", "<tokenType>", "null" and "any" 
#    And I post the details to approve provisioning endpoint
#    Then I verify the status code as "200"
#    And I verify that the given pan is declined for visa token provisioning as expected
#    And I verify that table entries are as expected after approve provisioning for "<tokenType>" and "<flowType>"
#    And I verify that entries are created for approve provisioning request in event and external log tables of Common logging service
#    And I verify that the response time is under the SLA for approve provisioning endpoint
#    Examples:
#      | panSource          | tokenType      | tokenRequester      | flowType | accountState |
#      | on_file            | hce            | Google Pay          | yellow   | DELETED      |
#      | key_entered        | hce            | Google Pay          | yellow   | DELETED      |
#      | mobile_banking_app | hce            | Google Pay          | yellow   | DELETED      |
#      | on_file            | secure_element | Apple pay           | yellow   | DELETED      |
#      | key_entered        | secure_element | Apple pay           | yellow   | DELETED      |
#      | mobile_banking_app | secure_element | Apple pay           | yellow   | DELETED      |
#      | on_file            | hce            | Google Pay          | yellow   | SUSPENDED    |
#      | key_entered        | hce            | Google Pay          | yellow   | SUSPENDED    |
#      | mobile_banking_app | hce            | Google Pay          | yellow   | SUSPENDED    |
#      | on_file            | secure_element | Apple pay           | yellow   | SUSPENDED    |
#      | key_entered        | secure_element | Apple pay           | yellow   | SUSPENDED    |
#      | mobile_banking_app | secure_element | Apple pay           | yellow   | SUSPENDED    |
#
#  @Iteration23.4 @createAccountOrUpdateAccountState @APRedAfterCENonActiveForAPGP
#  Scenario Outline: Scenario 16 - Verify that the token provisioning is declined after verifying check eligibility for pan with other than active state when token requester is either google pay or apple pay
#    Given Pre-requisite: I verify check eligibility for given PAN when token requester as "<tokenRequester>", account state as "<accountState>", profile id as "any", account ref type as "CARDID", idvMethod as "any" and pan source as "<panSource>" with "no" null field
#    Given I encrypt PAN for approve provisioning when token requester as "<tokenRequester>", account state as "ACTIVE", profile id as "any", account ref type as "CARDID", idvMethod as "any" and approval based on "riskAssessmentScore" for "<flowType>" flow with "no" null field
#    And I have the default VTS headers
#    When I have the approve provisioning request body as defined "<panSource>", "<tokenType>", "null" and "any" 
#    And I post the details to approve provisioning endpoint
#    Then I verify the status code as "200"
#    And I verify that the given pan is declined for visa token provisioning as expected
#    And I verify that table entries are as expected after approve provisioning for "<tokenType>" and "<flowType>"
#    And I verify that entries are created for approve provisioning request in event and external log tables of Common logging service
#    And I verify that the response time is under the SLA for approve provisioning endpoint
#    Examples:
#      | panSource          | tokenType      | tokenRequester      | flowType | accountState |
#      | on_file            | hce            | Google Pay          | yellow   | DELETED      |
#      | key_entered        | hce            | Google Pay          | yellow   | DELETED      |
#      | mobile_banking_app | hce            | Google Pay          | yellow   | DELETED      |
#      | on_file            | secure_element | Apple pay           | yellow   | DELETED      |
#      | key_entered        | secure_element | Apple pay           | yellow   | DELETED      |
#      | mobile_banking_app | secure_element | Apple pay           | yellow   | DELETED      |
#      | on_file            | hce            | Google Pay          | yellow   | SUSPENDED    |
#      | key_entered        | hce            | Google Pay          | yellow   | SUSPENDED    |
#      | mobile_banking_app | hce            | Google Pay          | yellow   | SUSPENDED    |
#      | on_file            | secure_element | Apple pay           | yellow   | SUSPENDED    |
#      | key_entered        | secure_element | Apple pay           | yellow   | SUSPENDED    |
#      | mobile_banking_app | secure_element | Apple pay           | yellow   | SUSPENDED    |

  @Iteration23.4 @VTS_E2E_APYellowByRskScoreForAPGP
  @GetToken
  @InAppPayload
  @UpdateAccountState
  @UpdateVirtualAccountState
  @GetAccountAndVirtualAccount
  @UpdateProfile
  Scenario Outline: Scenario 17A - Verify that the token provisioning is processed successfully while approve provisioning approved based on riskAssessmentScore
    Given Pre-requisite: I verify check eligibility for given PAN when token requester as "<tokenRequester>", account state as "ACTIVE", profile id as "any", account ref type as "CARDID", idvMethod as "any" and pan source as "<panSource>" with "no" null field
    When I verify that approve provisioning is successful for given PAN when token requester as "<tokenRequester>", account state as "ACTIVE", profile id as "any", account ref type as "CARDID", idvMethod as "any", pan source as "<panSource>", token type as "<tokenType>", approval based on "riskAssessmentScore" for "<flowType>" flow with "no" null field and action code expected is "00"
    And I verify that create token is done for the given account details "<panSource>", "<tokenType>" and "green"
    Then I verify that update token is done for the given account details "ACTIVE", "<messageReasonCode>", "green" and "<tokenType>"
    Examples:
      | panSource          | tokenType      | tokenRequester | flowType | messageReasonCode    |
      | mobile_banking_app | hce            | Google Pay     | yellow   | token_status_updated |
      | mobile_banking_app | secure_element | Apple pay      | yellow   | token_status_updated |

    @Iteration23.4 @VTS_E2E_APYellowByRskScoreForAPGP
    @GetToken
    @InAppPayload
    @UpdateAccountState
    @UpdateVirtualAccountState
    @GetAccountAndVirtualAccount
    @UpdateProfile
  Scenario Outline: Scenario 17B - Verify that the token provisioning is processed successfully while approve provisioning approved based on riskAssessmentScore
    Given Pre-requisite: I verify check eligibility for given PAN when token requester as "<tokenRequester>", account state as "ACTIVE", profile id as "any", account ref type as "CARDID", idvMethod as "any" and pan source as "<panSource>" with "no" null field
    When I verify that approve provisioning is successful for given PAN when token requester as "<tokenRequester>", account state as "ACTIVE", profile id as "any", account ref type as "CARDID", idvMethod as "any", pan source as "<panSource>", token type as "<tokenType>", approval based on "riskAssessmentScore" for "<flowType>" flow with "no" null field and action code expected is "85"
    And I verify that create token is done for the given account details "<panSource>", "<tokenType>" and "<flowType>"
    Then I verify that update token is done for the given account details "ACTIVE", "<messageReasonCode>", "<flowType>" and "<tokenType>"
    Examples:
      | panSource          | tokenType      | tokenRequester | flowType | messageReasonCode    |
      | on_file            | hce            | Google Pay     | yellow   | token_status_updated |
      | key_entered        | hce            | Google Pay     | yellow   | token_status_updated |
      | on_file            | secure_element | Apple pay      | yellow   | token_status_updated |
      | key_entered        | secure_element | Apple pay      | yellow   | token_status_updated |

  @Iteration23.4 @VTS_E2E_APByWPRCForAP
  @GetToken
  @InAppPayload
  @UpdateAccountState
  @UpdateVirtualAccountState
  @GetAccountAndVirtualAccount
  @UpdateProfile
  Scenario Outline: Scenario 18 - Verify that the token provisioning is processed successfully for apple pay where pan source is mobile_banking_app and  walletProviderReasonCodes is non 0G
    Given Pre-requisite: I verify check eligibility for given PAN when token requester as "<tokenRequester>", account state as "ACTIVE", profile id as "any", account ref type as "CARDID", idvMethod as "any" and pan source as "<panSource>" with "no" null field
    When I verify that approve provisioning is successful for given PAN when token requester as "<tokenRequester>", account state as "ACTIVE", profile id as "any", account ref type as "CARDID", idvMethod as "any", pan source as "<panSource>", token type as "<tokenType>", approval based on "walletProviderReasonCodes" for "<flowType>" flow with "no" null field and action code expected is "00"
    And I verify that create token is done for the given account details "<panSource>", "<tokenType>" and "<flowType>"
    Then I verify that update token is done for the given account details "ACTIVE", "<messageReasonCode>", "<flowType>" and "<tokenType>"
    Examples:
      | panSource          | tokenType      | tokenRequester | flowType | messageReasonCode    |
      | mobile_banking_app | secure_element | Apple pay      | green    | token_status_updated |

  @Iteration23.4 @VTS_E2E_APYellowByRskScoreForNonAPGP_DB
  @GetToken
  @UpdateAccountState
  @UpdateVirtualAccountState
  @GetAccountAndVirtualAccount
  @UpdateProfile
  Scenario Outline: Scenario 19 - Verify that the token provisioning is processed successfully and single device binding is as expected for COF and ECOM while approve provisioning approved based on riskAssessmentScore
    Given Pre-requisite: I verify check eligibility for given PAN when token requester as "<tokenRequester>", account state as "ACTIVE", profile id as "any", account ref type as "CARDID", idvMethod as "any" and pan source as "<panSource>" with "no" null field
    When I verify that approve provisioning is successful for given PAN when token requester as "<tokenRequester>", account state as "ACTIVE", profile id as "any", account ref type as "CARDID", idvMethod as "any", pan source as "<panSource>", token type as "<tokenType>", approval based on "riskAssessmentScore" for "<flowType>" flow with "no" null field and action code expected is "00"
    And I verify that create token is done for the given account details "<panSource>", "<tokenType>" and "<flowType>"
    And I verify that update token is done for the given account details "<tokenStatus>", "<messageReasonCode1>", "<flowType>" and "<tokenType>"
    Then I verify that device binding is done for the given account details
    And I verify that get CVM is done for the given account details
    And I verify that send passcode is done for the given account details with otp identifier as "<otpIdentifier>"
    Then I verify that update token is done for the given account details "<tokenStatus>", "<messageReasonCode2>", "<flowType>" and "<tokenType>"
    And I verify that update token is done for the given account details "<tokenStatus>", "<messageReasonCode3>", "<flowType>" and "<tokenType>"
    Examples:
      | panSource          | tokenType    | tokenRequester      | flowType | tokenStatus | messageReasonCode1   | messageReasonCode2          | messageReasonCode3      | otpIdentifier     |
      | on_file            | card_on_file | define_at_runtime   | green    | ACTIVE      | token_status_updated | token_device_binding_result | otp_verification_result | define_at_runtime |
#      | key_entered        | card_on_file | define_at_runtime   | green    | ACTIVE      | token_status_updated | token_device_binding_result | otp_verification_result | define_at_runtime |
#      | mobile_banking_app | card_on_file | define_at_runtime   | green    | ACTIVE      | token_status_updated | token_device_binding_result | otp_verification_result | define_at_runtime |
#      | on_file            | ecommerce    | define_at_runtime   | green    | ACTIVE      | token_status_updated | token_device_binding_result | otp_verification_result | define_at_runtime |
#      | key_entered        | ecommerce    | define_at_runtime   | green    | ACTIVE      | token_status_updated | token_device_binding_result | otp_verification_result | define_at_runtime |
#      | mobile_banking_app | ecommerce    | define_at_runtime   | green    | ACTIVE      | token_status_updated | token_device_binding_result | otp_verification_result | define_at_runtime |
#      | on_file            | card_on_file | fetch_from_database | green    | ACTIVE      | token_status_updated | token_device_binding_result | otp_verification_result | define_at_runtime |
#      | key_entered        | card_on_file | fetch_from_database | green    | ACTIVE      | token_status_updated | token_device_binding_result | otp_verification_result | define_at_runtime |
#      | mobile_banking_app | card_on_file | fetch_from_database | green    | ACTIVE      | token_status_updated | token_device_binding_result | otp_verification_result | define_at_runtime |
#      | on_file            | ecommerce    | fetch_from_database | green    | ACTIVE      | token_status_updated | token_device_binding_result | otp_verification_result | define_at_runtime |
#      | key_entered        | ecommerce    | fetch_from_database | green    | ACTIVE      | token_status_updated | token_device_binding_result | otp_verification_result | define_at_runtime |
#      | mobile_banking_app | ecommerce    | fetch_from_database | green    | ACTIVE      | token_status_updated | token_device_binding_result | otp_verification_result | define_at_runtime |

  @Iteration23.4 @VTS_E2E_DBFail_NonActive
  @VTS_E2E_DB_NonActive
  @GetToken
  @UpdateAccountState
  @UpdateVirtualAccountState
  @GetAccountAndVirtualAccount
  @UpdateProfile
  Scenario Outline: Scenario 20 - Verify that the token provisioning is processed successfully but device binding is unsuccessful when the token is on other than active state
    Given Pre-requisite: I verify check eligibility for given PAN when token requester as "<tokenRequester>", account state as "ACTIVE", profile id as "any", account ref type as "CARDID", idvMethod as "any" and pan source as "<panSource>" with "no" null field
    When I verify that approve provisioning is successful for given PAN when token requester as "<tokenRequester>", account state as "ACTIVE", profile id as "any", account ref type as "CARDID", idvMethod as "any", pan source as "<panSource>", token type as "<tokenType>", approval based on "riskAssessmentScore" for "<flowType>" flow with "no" null field and action code expected is "00"
    And I verify that create token is done for the given account details "<panSource>", "<tokenType>" and "<flowType>"
    Then I initiate device binding for the given account details
    And I verify the status code as "200"
    And I verify the error code "<errorCode>" in the response
    Then I verify that update token is done for the given account details "ACTIVE", "token_status_updated", "<flowType>" and "<tokenType>"
    Then I verify that update token is done for the given account details "SUSPENDED", "TOKEN_SUSPEND", "<flowType>" and "<tokenType>"
    And I initiate device binding for the given account details
    And I verify the status code as "200"
    And I verify the error code "<errorCode>" in the response
    Then I verify that update token is done for the given account details "ACTIVE", "token_status_updated", "<flowType>" and "<tokenType>"
    Then I verify that update token is done for the given account details "DEACTIVATED", "token_deactivated", "<flowType>" and "<tokenType>"
    And I initiate device binding for the given account details
    And I verify the status code as "200"
    And I verify the error code "<errorCode>" in the response
    Examples:
      | panSource          | tokenType    | tokenRequester      | flowType | errorCode |
      | on_file            | card_on_file | define_at_runtime   | green    | ISE40011  |
      | key_entered        | card_on_file | define_at_runtime   | green    | ISE40011  |
      | mobile_banking_app | card_on_file | define_at_runtime   | green    | ISE40011  |
      | on_file            | ecommerce    | define_at_runtime   | green    | ISE40011  |
      | key_entered        | ecommerce    | define_at_runtime   | green    | ISE40011  |
      | mobile_banking_app | ecommerce    | define_at_runtime   | green    | ISE40011  |
      | on_file            | card_on_file | fetch_from_database | green    | ISE40011  |
      | key_entered        | card_on_file | fetch_from_database | green    | ISE40011  |
      | mobile_banking_app | card_on_file | fetch_from_database | green    | ISE40011  |
      | on_file            | ecommerce    | fetch_from_database | green    | ISE40011  |
      | key_entered        | ecommerce    | fetch_from_database | green    | ISE40011  |
      | mobile_banking_app | ecommerce    | fetch_from_database | green    | ISE40011  |

  @Iteration23.4 @VTS_E2E_DBFail_ForAPGP
  @GetToken
  @InAppPayload
  @UpdateAccountState
  @UpdateVirtualAccountState
  @GetAccountAndVirtualAccount
  @UpdateProfile
  Scenario Outline: Scenario 21 - Verify that the token provisioning is processed successfully but device binding is unsuccessful for apple pay and google pay wallets
    Given Pre-requisite: I verify check eligibility for given PAN when token requester as "<tokenRequester>", account state as "ACTIVE", profile id as "any", account ref type as "CARDID", idvMethod as "any" and pan source as "<panSource>" with "no" null field
    When I verify that approve provisioning is successful for given PAN when token requester as "<tokenRequester>", account state as "ACTIVE", profile id as "any", account ref type as "CARDID", idvMethod as "any", pan source as "<panSource>", token type as "<tokenType>", approval based on "riskAssessmentScore" for "<flowType>" flow with "no" null field and action code expected is "85"
    And I verify that create token is done for the given account details "<panSource>", "<tokenType>" and "<flowType>"
    And I verify that update token is done for the given account details "ACTIVE", "<messageReasonCode>", "<flowType>" and "<tokenType>"
    Then I initiate device binding for the given account details
    And I verify the status code as "200"
    And I verify the error code "<errorCode>" in the response
    Examples:
      | panSource          | tokenType      | tokenRequester | flowType | messageReasonCode    | errorCode |
      | on_file            | hce            | Google Pay     | yellow   | token_status_updated | ISE40011  |
      | key_entered        | hce            | Google Pay     | yellow   | token_status_updated | ISE40011  |
      | on_file            | secure_element | Apple pay      | yellow   | token_status_updated | ISE40011  |
      | key_entered        | secure_element | Apple pay      | yellow   | token_status_updated | ISE40011  |

  @Iteration23.5 @VTS_E2E_APOrangeByWPRCForAP
  @GetToken
  @InAppPayload
  @UpdateAccountState
  @UpdateVirtualAccountState
  @GetAccountAndVirtualAccount
  @UpdateProfile
  Scenario Outline: Scenario 22 - Verify that the token provisioning is processed successfully for apple pay wallet where pan source is mobile_banking_app and  walletProviderReasonCodes is 0G
    Given Pre-requisite: I verify check eligibility for given PAN when token requester as "<tokenRequester>", account state as "ACTIVE", profile id as "any", account ref type as "CARDID", idvMethod as "any" and pan source as "<panSource>" with "no" null field
    When I verify that approve provisioning is successful for given PAN when token requester as "<tokenRequester>", account state as "ACTIVE", profile id as "any", account ref type as "CARDID", idvMethod as "any", pan source as "<panSource>", token type as "<tokenType>", approval based on "walletProviderReasonCodes" for "<flowType>" flow with "no" null field and action code expected is "85"
    And I verify that create token is done for the given account details "<panSource>", "<tokenType>" and "<flowType>"
    And I verify that get CVM is done for the given account details
    And I verify that send passcode is done for the given account details with otp identifier as "<otpIdentifier>"
    Then I verify that update token is done for the given account details "<tokenStatus>", "<messageReasonCode1>", "<flowType>" and "<tokenType>"
    And I verify that update token is done for the given account details "<tokenStatus>", "<messageReasonCode2>", "<flowType>" and "<tokenType>"
    Examples:
      | panSource          | tokenType      | tokenRequester | flowType | tokenStatus | otpIdentifier     | messageReasonCode1   | messageReasonCode2      |
      | mobile_banking_app | secure_element | Apple pay      | Orange   | ACTIVE      | define_at_runtime | token_status_updated | otp_verification_result |

  @Iteration23.5 @VTS_E2E_APYellowByRskScore
  @GetToken
  @InAppPayload
  @UpdateAccountState
  @UpdateVirtualAccountState
  @GetAccountAndVirtualAccount
  @UpdateProfile
  Scenario Outline: Scenario 23 - Verify that the token provisioning is processed successfully while approve provisioning required additional authentication based on riskAssessmentScore
    Given Pre-requisite: I verify check eligibility for given PAN when token requester as "<tokenRequester>", account state as "ACTIVE", profile id as "any", account ref type as "CARDID", idvMethod as "any" and pan source as "<panSource>" with "no" null field
    When I verify that approve provisioning is successful for given PAN when token requester as "<tokenRequester>", account state as "ACTIVE", profile id as "any", account ref type as "CARDID", idvMethod as "any", pan source as "<panSource>", token type as "<tokenType>", approval based on "riskAssessmentScore" for "<flowType>" flow with "no" null field and action code expected is "85"
    And I verify that create token is done for the given account details "<panSource>", "<tokenType>" and "<flowType>"
    And I verify that get CVM is done for the given account details
    And I verify that send passcode is done for the given account details with otp identifier as "<otpIdentifier>"
    Then I verify that update token is done for the given account details "<tokenStatus>", "<messageReasonCode1>", "<flowType>" and "<tokenType>"
    And I verify that update token is done for the given account details "<tokenStatus>", "<messageReasonCode2>", "<flowType>" and "<tokenType>"
    Examples:
      | panSource   | tokenType      | tokenRequester | flowType | tokenStatus | otpIdentifier     | messageReasonCode1   | messageReasonCode2      |
      | on_file     | hce            | Google Pay     | yellow   | ACTIVE      | define_at_runtime | token_status_updated | otp_verification_result |
      | key_entered | hce            | Google Pay     | yellow   | ACTIVE      | define_at_runtime | token_status_updated | otp_verification_result |
      | on_file     | secure_element | Apple pay      | yellow   | ACTIVE      | define_at_runtime | token_status_updated | otp_verification_result |
      | key_entered | secure_element | Apple pay      | yellow   | ACTIVE      | define_at_runtime | token_status_updated | otp_verification_result |

  @Iteration23.5 @VTS_E2E_APYellowByRskWltInfo
  @GetToken
  @InAppPayload
  @UpdateAccountState
  @UpdateVirtualAccountState
  @GetAccountAndVirtualAccount
  @UpdateProfile
  Scenario Outline: Scenario 24 - Verify that the token provisioning is processed successfully when risk and wallet info are null during approve provisioning
    Given Pre-requisite: I verify check eligibility for given PAN when token requester as "<tokenRequester>", account state as "ACTIVE", profile id as "any", account ref type as "CARDID", idvMethod as "any" and pan source as "<panSource>" with "walletRisks" null field
    When I verify that approve provisioning is successful for given PAN when token requester as "<tokenRequester>", account state as "ACTIVE", profile id as "any", account ref type as "CARDID", idvMethod as "any", pan source as "<panSource>", token type as "<tokenType>", approval based on "walletRisks" for "<flowType>" flow with "walletRisks" null field and action code expected is "85"
    And I verify that create token is done for the given account details "<panSource>", "<tokenType>" and "<flowType>"
    And I verify that get CVM is done for the given account details
    And I verify that send passcode is done for the given account details with otp identifier as "<otpIdentifier>"
    Then I verify that update token is done for the given account details "<tokenStatus>", "<messageReasonCode1>", "<flowType>" and "<tokenType>"
    And I verify that update token is done for the given account details "<tokenStatus>", "<messageReasonCode2>", "<flowType>" and "<tokenType>"
    Examples:
      | panSource   | tokenType      | tokenRequester | flowType | tokenStatus | otpIdentifier     | messageReasonCode1   | messageReasonCode2      |
      | on_file     | hce            | Google Pay     | yellow   | ACTIVE      | define_at_runtime | token_status_updated | otp_verification_result |
      | key_entered | hce            | Google Pay     | yellow   | ACTIVE      | define_at_runtime | token_status_updated | otp_verification_result |
      | on_file     | secure_element | Apple pay      | yellow   | ACTIVE      | define_at_runtime | token_status_updated | otp_verification_result |
      | key_entered | secure_element | Apple pay      | yellow   | ACTIVE      | define_at_runtime | token_status_updated | otp_verification_result |

  @Iteration23.5 @VTS_E2E_IncorrectOTPForAPGP
  @GetToken
  @InAppPayload
  @UpdateAccountState
  @GetAccountAndVirtualAccount
  @UpdateProfile
  Scenario Outline: Scenario 25 - Verify that the token provisioning is not processed for google pay and apple pay wallets due to incorrect otp identifier on send passcode
    Given Pre-requisite: I verify check eligibility for given PAN when token requester as "<tokenRequester>", account state as "ACTIVE", profile id as "any", account ref type as "CARDID", idvMethod as "any" and pan source as "<panSource>" with "walletRisks" null field
    When I verify that approve provisioning is successful for given PAN when token requester as "<tokenRequester>", account state as "ACTIVE", profile id as "any", account ref type as "CARDID", idvMethod as "any", pan source as "<panSource>", token type as "<tokenType>", approval based on "walletRisks" for "<flowType>" flow with "walletRisks" null field and action code expected is "85"
    And I verify that create token is done for the given account details "<panSource>", "<tokenType>" and "<flowType>"
    And I verify that get CVM is done for the given account details
    Then I verify that send passcode is failed for the given account details with invalid otp identifier as "<otpIdentifier>" and the error code is "<errorCode>"
    Examples:
      | panSource   | tokenType      | tokenRequester | flowType | otpIdentifier     | errorCode |
      | on_file     | hce            | Google Pay     | yellow   | define_at_runtime | ISE40011  |
      | key_entered | hce            | Google Pay     | yellow   | define_at_runtime | ISE40011  |
      | on_file     | secure_element | Apple pay      | yellow   | define_at_runtime | ISE40011  |
      | key_entered | secure_element | Apple pay      | yellow   | define_at_runtime | ISE40011  |

  @Iteration23.4 @VTS_E2E_IncorrectOTPForNonAPGP
  @GetToken
  @UpdateAccountState
  @GetAccountAndVirtualAccount
  @UpdateProfile
  Scenario Outline: Scenario 26 - Verify that the device binding is not processed for xpay wallets due to incorrect otp identifier on send passcode
    Given Pre-requisite: I verify check eligibility for given PAN when token requester as "<tokenRequester>", account state as "ACTIVE", profile id as "any", account ref type as "CARDID", idvMethod as "any" and pan source as "<panSource>" with "no" null field
    When I verify that approve provisioning is successful for given PAN when token requester as "<tokenRequester>", account state as "ACTIVE", profile id as "any", account ref type as "CARDID", idvMethod as "any", pan source as "<panSource>", token type as "<tokenType>", approval based on "riskAssessmentScore" for "<flowType>" flow with "no" null field and action code expected is "00"
    And I verify that create token is done for the given account details "<panSource>", "<tokenType>" and "<flowType>"
    And I verify that update token is done for the given account details "ACTIVE", "<messageReasonCode1>", "<flowType>" and "<tokenType>"
    Then I verify that device binding is done for the given account details
    And I verify that get CVM is done for the given account details
    Then I verify that send passcode is failed for the given account details with invalid otp identifier as "<otpIdentifier>" and the error code is "<errorCode>"
    Examples:
      | panSource          | tokenType    | tokenRequester      | flowType | messageReasonCode1   | otpIdentifier     | errorCode |
      | on_file            | card_on_file | define_at_runtime   | green    | token_status_updated | define_at_runtime | ISE40011  |
      | key_entered        | card_on_file | define_at_runtime   | green    | token_status_updated | define_at_runtime | ISE40011  |
      | mobile_banking_app | card_on_file | define_at_runtime   | green    | token_status_updated | define_at_runtime | ISE40011  |
      | on_file            | ecommerce    | define_at_runtime   | green    | token_status_updated | define_at_runtime | ISE40011  |
      | key_entered        | ecommerce    | define_at_runtime   | green    | token_status_updated | define_at_runtime | ISE40011  |
      | mobile_banking_app | ecommerce    | define_at_runtime   | green    | token_status_updated | define_at_runtime | ISE40011  |
      | on_file            | card_on_file | fetch_from_database | green    | token_status_updated | define_at_runtime | ISE40011  |
      | key_entered        | card_on_file | fetch_from_database | green    | token_status_updated | define_at_runtime | ISE40011  |
      | mobile_banking_app | card_on_file | fetch_from_database | green    | token_status_updated | define_at_runtime | ISE40011  |
      | on_file            | ecommerce    | fetch_from_database | green    | token_status_updated | define_at_runtime | ISE40011  |
      | key_entered        | ecommerce    | fetch_from_database | green    | token_status_updated | define_at_runtime | ISE40011  |
      | mobile_banking_app | ecommerce    | fetch_from_database | green    | token_status_updated | define_at_runtime | ISE40011  |

  @Iteration23.5 @VTS_E2E_APP2APP_IDV
  @VTS_SavingsBankFI
  @SkipTestCards
  @GetToken
  @InAppPayload
  @UpdateAccountState
  @GetAccountAndVirtualAccount
  @UpdateProfile
  Scenario Outline: Scenario 27 - Verify that the token provisioning is processed till cardholder verification when the idv method is set as app_2_app
    Given Pre-requisite: I verify check eligibility for given PAN when token requester as "<tokenRequester>", account state as "ACTIVE", profile id as "any", account ref type as "null", idvMethod as "app2app" and pan source as "<panSource>" with "walletRisks" null field
    When I verify that approve provisioning is successful for given PAN when token requester as "<tokenRequester>", account state as "ACTIVE", profile id as "any", account ref type as "null", idvMethod as "app2app", pan source as "<panSource>", token type as "<tokenType>", approval based on "walletRisks" for "<flowType>" flow with "walletRisks" null field and action code expected is "85"
    Then I verify that create token is done for the given account details "<panSource>", "<tokenType>" and "<flowType>"
    And I verify that get CVM is done for the given account details
    Examples:
      | panSource   | tokenType      | tokenRequester | flowType |
      | on_file     | hce            | Google Pay     | yellow   |
      | key_entered | hce            | Google Pay     | yellow   |
      | on_file     | secure_element | Apple pay      | yellow   |
      | key_entered | secure_element | Apple pay      | yellow   |

  @Iteration23.5 @VTS_E2E_CustomerCare_IDV
  @VTS_Bonum
  @SkipTestCards
  Scenario Outline: Scenario 28 - Verify that the token provisioning is processed only till cardholder verification when the idv method is set as customer care
    Given Pre-requisite: I verify check eligibility for given PAN when token requester as "<tokenRequester>", account state as "ACTIVE", profile id as "null", account ref type as "CARDID", idvMethod as "customercare" and pan source as "<panSource>" with "walletRisks" null field
    When I verify that approve provisioning is successful for given PAN when token requester as "<tokenRequester>", account state as "ACTIVE", profile id as "null", account ref type as "CARDID", idvMethod as "customercare", pan source as "<panSource>", token type as "<tokenType>", approval based on "walletRisks" for "<flowType>" flow with "walletRisks" null field and action code expected is "85"
    Then I verify that create token is done for the given account details "<panSource>", "<tokenType>" and "<flowType>"
    And I verify that get CVM is done for the given account details
    Examples:
      | panSource   | tokenType      | tokenRequester | flowType |
      | on_file     | hce            | Google Pay     | yellow   |
      | key_entered | hce            | Google Pay     | yellow   |
      | on_file     | secure_element | Apple pay      | yellow   |
      | key_entered | secure_element | Apple pay      | yellow   |

#  @Iteration23.5 @VTS_E2E_RenewReplace
#  @SkipTestCards
#  @RenewReplace
#  Scenario Outline: Scenario 29 - Verify that Renew and Replace are as expected after token provisioning
#    Given Pre-requisite: I verify check eligibility for given PAN when token requester as "<tokenRequester>", account state as "ACTIVE", profile id as "any", account ref type as "CARDID", idvMethod as "any" and pan source as "<panSource>" with "walletRisks" null field
#    When I verify that approve provisioning is successful for given PAN when token requester as "<tokenRequester>", account state as "ACTIVE", profile id as "any", account ref type as "CARDID", idvMethod as "any", pan source as "<panSource>", token type as "<tokenType>", approval based on "riskAssessmentScore" for "<flowType>" flow with "walletRisks" null field and action code expected is "85"
#    And I verify that create token is done for the given account details "<panSource>", "<tokenType>" and "<flowType>"
#    And I verify that get CVM is done for the given account details
#    And I verify that send passcode is done for the given account details with otp identifier as "<otpIdentifier>"
#    Then I verify that update token is done for the given account details "<tokenStatus>", "<messageReasonCode1>", "<flowType>" and "<tokenType>"
#    Examples:
#      | panSource          | tokenType      | tokenRequester | flowType | tokenStatus | otpIdentifier     | messageReasonCode1   |
#      | on_file            | hce            | Google Pay     | yellow   | ACTIVE      | define_at_runtime | token_status_updated |
#      | key_entered        | hce            | Google Pay     | yellow   | ACTIVE      | define_at_runtime | token_status_updated |
#      | mobile_banking_app | hce            | Google Pay     | yellow   | ACTIVE      | define_at_runtime | token_status_updated |
#      | on_file            | secure_element | Apple pay      | yellow   | ACTIVE      | define_at_runtime | token_status_updated |
#      | key_entered        | secure_element | Apple pay      | yellow   | ACTIVE      | define_at_runtime | token_status_updated |
#      | mobile_banking_app | secure_element | Apple pay      | yellow   | ACTIVE      | define_at_runtime | token_status_updated |

  @Iteration26.1 @APGreenBySimVerification
  @VTS_Comdirect
  Scenario Outline: Scenario 29 - Verify that the token provisioning is approved when sim verification enabled for apple pay
    Given I encrypt PAN for approve provisioning when token requester as "<tokenRequester>", account state as "ACTIVE", profile id as "any", account ref type as "CARDID", idvMethod as "any" and approval based on "riskAssessmentScore" for "yellow" flow with "no" null field
    And I have the default VTS headers
    When I have the approve provisioning request body as defined "<panSource>", "<tokenType>", "<deviceType>" and "any"
    And I post the details to approve provisioning endpoint
    Then I verify the status code as "200"
    And I verify that the given pan is approved for visa token provisioning as expected with action code as "00"
    And I verify that table entries are as expected after approve provisioning for "<tokenType>" and "<flowType>"
    And I verify that the response time is under the SLA for approve provisioning endpoint
    Examples:
      | panSource   | tokenType      | deviceType   | tokenRequester | flowType |
      | key_entered | secure_element | mobile_phone | Apple pay      | green    |

  @Iteration26.1 @APYellowBySimVerification
  @VTS_Comdirect
  Scenario Outline: Scenario 30 - Verify that the token provisioning is required additional authentication when sim verification enabled but there are some invalid request fields
    Given I encrypt PAN for approve provisioning when token requester as "<tokenRequester>", account state as "ACTIVE", profile id as "any", account ref type as "CARDID", idvMethod as "any" and approval based on "riskAssessmentScore" for "<flowType>" flow with "no" null field
    And I have the default VTS headers
    When I have the approve provisioning request body as defined "<panSource>", "<tokenType>", "<deviceType>" and "any"
    And I post the details to approve provisioning endpoint
    Then I verify the status code as "200"
    And I verify that the given pan is approved for visa token provisioning as expected with action code as "85"
    And I verify that table entries are as expected after approve provisioning for "<tokenType>" and "<flowType>"
    And I verify that the response time is under the SLA for approve provisioning endpoint
    Examples:
      | panSource   | tokenType      | deviceType            | tokenRequester | flowType |
      | on_file     | secure_element | mobile_phone          | Apple pay      | yellow   |
      | key_entered | hce            | mobile_phone          | Google Pay     | yellow   |
      | key_entered | secure_element | mobilephone_or_tablet | Apple pay      | yellow   |
      | key_entered | secure_element | unknown               | Apple pay      | yellow   |
      | key_entered | secure_element | tablet                | Apple pay      | yellow   |
      | key_entered | secure_element | watch                 | Apple pay      | yellow   |
      | key_entered | secure_element | pc                    | Apple pay      | yellow   |
      | key_entered | secure_element | null                  | Apple pay      | yellow   |

  @Iteration26.2 @APYellowBySimVerification
  @VTS_Comdirect
  Scenario Outline: Scenario 31 - Verify that the token provisioning is required additional authentication when sim verification enabled for apple pay but the mobile number is not matched with VISA scheme
    Given I encrypt PAN for approve provisioning when token requester as "<tokenRequester>", account state as "ACTIVE", profile id as "any", account ref type as "CARDID", idvMethod as "any" and approval based on "riskAssessmentScore" for "<flowType>" flow with "no" null field
    And I have the default VTS headers
    When I have the approve provisioning request body as defined "<panSource>", "<tokenType>", "<deviceType>" and "any"
    And I update invalid mobile number as device number in approve provisioning request
    And I post the details to approve provisioning endpoint
    Then I verify the status code as "200"
    And I verify that the given pan is approved for visa token provisioning as expected with action code as "85"
    And I verify that table entries are as expected after approve provisioning for "<tokenType>" and "<flowType>"
    And I verify that the response time is under the SLA for approve provisioning endpoint
    Examples:
      | panSource   | tokenType      | deviceType   | tokenRequester | flowType |
      | key_entered | secure_element | mobile_phone | Apple pay      | yellow   |

  @Iteration26.2 @APYellowBySimVerification
  @VTS_LunarBankDk
  Scenario Outline: Scenario 32 - Verify that the token provisioning is required additional authentication when sim verification disabled for apple pay
    Given I encrypt PAN for approve provisioning when token requester as "<tokenRequester>", account state as "ACTIVE", profile id as "any", account ref type as "CARDID", idvMethod as "any" and approval based on "riskAssessmentScore" for "<flowType>" flow with "no" null field
    And I have the default VTS headers
    When I have the approve provisioning request body as defined "<panSource>", "<tokenType>", "<deviceType>" and "any" 
    And I post the details to approve provisioning endpoint
    Then I verify the status code as "200"
    And I verify that the given pan is approved for visa token provisioning as expected with action code as "85"
    And I verify that table entries are as expected after approve provisioning for "<tokenType>" and "<flowType>"
    And I verify that the response time is under the SLA for approve provisioning endpoint
    Examples:
      | panSource   | tokenType      | deviceType   | tokenRequester | flowType |
      | key_entered | secure_element | mobile_phone | Apple pay      | yellow   |

#  @Iteration26.2 @APYellowBySimVerification
#  @SkipTestCards @VTS_Comdirect
#  Scenario Outline: Scenario 33 - Verify that the token provisioning is required additional authentication when sim verification enabled for apple pay but no sms idv present
#    Given I encrypt PAN for approve provisioning when token requester as "<tokenRequester>", account state as "ACTIVE", profile id as "any", account ref type as "CARDID", idvMethod as "email" and approval based on "riskAssessmentScore" for "<flowType>" flow with "no" null field
#    And I have the default VTS headers
#    When I have the approve provisioning request body as defined "<panSource>", "<tokenType>", "<deviceType>" and "email"
#    And I post the details to approve provisioning endpoint
#    Then I verify the status code as "200"
#    And I verify that the given pan is approved for visa token provisioning as expected with action code as "85"
#    And I verify that table entries are as expected after approve provisioning for "<tokenType>" and "<flowType>"
#    And I verify that the response time is under the SLA for approve provisioning endpoint
#    Examples:
#      | panSource   | tokenType      | deviceType   | tokenRequester | flowType |
#      | key_entered | secure_element | mobile_phone | Apple pay      | yellow   |