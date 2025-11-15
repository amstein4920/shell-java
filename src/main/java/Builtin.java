enum Builtin {
    EXIT,
    ECHO,
    CD,
    PWD,
    TYPE;

    public static Boolean isBuiltin(String input) {
        for (Builtin builtin : Builtin.values()) {
            if (builtin.name().equals(input.toUpperCase())) {
                return true;
            }
        }
        return false;
    }
}
