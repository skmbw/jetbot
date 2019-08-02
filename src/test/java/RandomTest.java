import java.util.Random;

/**
 * @author yinlei
 * @since 2019-8-2 14:01
 */
public class RandomTest {

    public static void main(String[] args) {
        Random random = new Random(10);
        for (int j = 0; j < 25; j++) {
            int i = random.nextInt(12);
            System.out.println(i);
        }
    }
}
