import java.util.Arrays;

public class MathParser {

    public static void main(String[] args){
        System.out.println("Erfolgreicher Aufruf: ");
        System.out.println(parseSum("1+2+3+4+5+6+7+8+9+10"));
        System.out.println("Fehlerhafter Aufruf: ");
        System.out.println(parseSum("1-5"));
    }

    private static int parseSum(String input) throws IllegalArgumentException{
        try{
            return Arrays.stream(input.split("\\+")).mapToInt(Integer::parseInt).sum();
        } catch (IllegalArgumentException e){
           throw new IllegalArgumentException("Wrong input: " + input + ". Input should be of type x+y+z+...+n");
        }
    }

}
