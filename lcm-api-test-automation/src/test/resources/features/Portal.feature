@PortalTests
Feature:Portal


  @SmokeTests @Iteration25.1 @AbuTspFlowDevice
  Scenario Outline: 1.1 - Verify that user can view the Device tokens for Searching PAN of TSP and ABU subscribed accounts in Portal UI
    Given I checked the Funding Pan added for the IssuerId for device Token "<LCMService>","<TokenType>"
    When I Login to Portal with valid credentials
    Then I Search the funding PAN in Portal and check the device view for "<LCMService>"
    And I verify the token info and device info
    And I verify the Portal data funding pan for device token
    Examples:
      | LCMService | TokenType |
      | TSP        | Device    |
      | ABU        | Device    |
      | TSP,ABU    | Device    |

  @SmokeTests @Iteration25.2 @AbuTspFlowMerchant
  Scenario Outline: 1.2 - Verify that user can view the Merchant tokens for Searching PAN of TSP and ABU subscribed accounts in Portal UI
    Given I checked the Funding Pan added for the IssuerId for merchant Token "<LCMService>","<TokenType>"
    When I Login to Portal with valid credentials
    Then I Search the funding Pan in portal and check the merchant view for "<LCMService>"
    And I verify the token info and merchant info
    And I verify the Portal data funding pan for merchant token
    Examples:
      | LCMService | TokenType |
      | TSP        | Merchant  |
      | ABU        | Merchant  |
      | TSP,ABU    | Merchant  |

#  @SmokeTests @Iteration25.2 @TokenLevelAccess
#  Scenario Outline: 1.3 - Verify that when user search the funding pan then each token level action should be able to perform like Activate,Suspend and Delete in the device view of the portal
#    Given I checked the Funding Pan added for the IssuerId for device Token "<LCMService>","<TokenType>"
#    When I Login to Portal with valid credentials
#    Then I search the funding Pan and checked the device view
#    And I perform the device level actions in individual token
#    And I Verfiy The Device view data after performed the each token level actions
#
#    Examples:
#      | LCMService | TokenType |
#      | TSP        | Device    |
#      | ABU        | Device    |
#      | TSP,ABU    | Device    |
