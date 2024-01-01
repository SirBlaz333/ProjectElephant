Feature: HomePage Feature
  This feature deal with the all components of your application

  Scenario: Verify Homepage  of the application
    Given I navigate to the home page
    When Navigate Forgot password link
    Then I should see forgot password page
