import Proxy.JDKProxy;

public class ClientTest {
    public static void main(String[] args) {
        JDKProxy jdkProxy = new JDKProxy();
        Test test = jdkProxy.getProxy(Test.class);
        int res = test.maxArea(new int[]{1,8,6,2,5,4,8,3,7});
        String s = test.longestPalindrome("babad");
        System.out.println(res);
        System.out.println(s);
    }
}
