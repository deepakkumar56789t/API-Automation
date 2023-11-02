@VCardTests
Feature: VCard (SDD)

  @Iteration21.1 @SmokeTests
  Scenario Outline: 1.1 - Verify that there is an image profile id created for a valid data set
    Given I create a valid bearer token for VCard service
    And I have the issuer specific headers as defined in "<issuer>"
    And I have the retrieve card image id request body as defined in "<validRequestBodies>", "<accountType>", "<issuer>" and "<imageProfile>"
    When I post the details to retrieve card image id endpoint
    Then I verify the status code as "200"
    And Verify that the response has a valid card image ID
    And I verify the details on Account Info table in database
    And I verify that get card image id entries are created to event and external logs of Common logging service
    Examples:
      | validRequestBodies                   | accountType | issuer   | imageProfile |
      | valid/body/valid_request_body_PAN    | PAN         | EIKA     |              |
      | valid/body/valid_request_body_PANREF | PANREF      | PayAlly  | Expiry       |
#      | valid/body/valid_request_body_1.1 | PAN         | IKANO  | PANExpiry    |
#      | valid/body/valid_request_body_1.1 | CARDID      | IKANO  | PANExpiry    |
