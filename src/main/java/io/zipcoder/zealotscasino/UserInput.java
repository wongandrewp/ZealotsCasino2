package io.zipcoder.zealotscasino;

import java.io.InputStream;
import java.util.Scanner;
import org.apache.commons.lang3.math.NumberUtils;

public class UserInput {

    static Scanner in;
    UserInput(InputStream scannerIn){
        in = new Scanner(scannerIn);
    }
    UserInput(){
        in = new Scanner(System.in);
    }
    public static String getStringInput(String prompt) {
        UserInput.display(prompt);
        String userInput = in.nextLine();
        return userInput;
    }

    public static int getIntInput(String prompt) {
        boolean flag = true;
        int result = 0;
        while(flag){
            if(NumberUtils.isNumber(getStringInput(prompt))){
                result = Integer.parseInt(getStringInput(prompt));
                flag = false;
            } else display("Please input a valid amount");
        }
        return result;
    }

    public static Double getDoubleInput(String prompt) {
        try {
            return Double.parseDouble(getStringInput(prompt)); //needs to take double to two places
        } catch(IllegalArgumentException e){
            return getDoubleInput("Please input a valid number"); // Recursively call the method with a prompt describing the issue.
        }
    }

    public static void display(Object output) {
        System.out.println(output.toString());
    }
}
