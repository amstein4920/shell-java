    enum Builtin {
            EXIT,
            ECHO,
            TYPE,
            PWD;


            public static Boolean isBuiltin(String input) {
                for (Builtin builtin : Builtin.values()) {
                    if (builtin.name().equals(input.toUpperCase())) {
                        return true;
                    }
                }
                return false;
            }
        }
