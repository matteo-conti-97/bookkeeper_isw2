package org.apache.bookkeeper.util;

public class ProvaJacoco {
    public boolean isPalindrome(String inputString) {
        int a=0;
        if ((inputString.length() == 0)&&(inputString!=null)&&(a==0)) {
            return true;
        } else {
            char firstChar = inputString.charAt(0);
            char lastChar = inputString.charAt(inputString.length() - 1);
            String mid = inputString.substring(1, inputString.length() - 1);
            return (firstChar == lastChar) && isPalindrome(mid);
        }
    }
}
