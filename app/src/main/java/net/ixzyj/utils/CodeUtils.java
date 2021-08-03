package net.ixzyj.utils;

public class CodeUtils {
    // 检验时 用的正则    xxx-xxxxxxx 最后一位是校验码
    public static String CODE_REGEX_CC = "^[0-9][0-9][0-9]-\\d{6}[0-9X]$";
    // 计算校验码时的正则，传入的参数应该是 xxx-xxxxxx
    public static String CODE_REGEX = "^[0-9][0-9][0-9]-\\d{6}$";

    // 加权因子表 [2, 0, 1, 1, 2, 3, 4, 5, 6]
    public static int[] factors = new int[]{7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10, 5, 8, 4, 2};
    // 校验码表
    public static char[] ckcodes = new char[]{'1', '0', 'X', '9', '8', '7', '6', '5', '4', '3', '2'};

    /**
     * 扫码字串添加校验码
     *
     * @param elsCode 扫码字串
     * @return 追加校验的扫码字串
     */
    public static String genElscodeCkCode(String elsCode) {
        if (!elsCode.matches(CODE_REGEX)) {
            return "";
        }

        int copulas = 0;
        String code = elsCode.replace("-", "");
        for (int i = 0; i < code.length(); i++) {
            copulas += Character.getNumericValue(code.charAt(i)) * factors[i];
        }

        String ck = String.valueOf(ckcodes[copulas % 11]).toUpperCase();
        return elsCode + ck;
    }

    /**
     * @param code 含有校验码的扫码字串
     * @return 校验成功返回 true,失败返回 fFalse
     */
    public static boolean isValidCode(String code) {
        if (!code.matches(CODE_REGEX_CC)) {
            return false;
        }

        int copulas = 0;
        code = code.replace("-", "");
        for (int i = 0; i < code.length() - 1; i++) {
            copulas += Character.getNumericValue(code.charAt(i)) * factors[i];
        }

        char ck = Character.toUpperCase(ckcodes[copulas % 11]);
        return code.charAt(code.length() - 1) == ck;
    }

    public static void main(String[] args) {
        // 生成校验码并验证
        String[] elsCodes = {"201-123456", "111-124456", "111-1244563"};
        for (String elsCode: elsCodes) {
            System.out.print(elsCode + ", ");
            String code = genElscodeCkCode(elsCode);
            System.out.print(code + ", ");
            System.out.println(isValidCode(code));
        }

        // 验证带校验码的扫码结果
        String[] codes = {"201-1234561", "101-124456X", "100-0000024"};
        for (String code: codes) {
            System.out.println(isValidCode(code));
        }    }
}
