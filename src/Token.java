class Token {
    TokenName name;
    String value;

    Token(TokenName name, String value) {
        this.name = name;
        this.value = value;
    }

    @Override
    public String toString() {
        if (value.length() != 0)
            return name + " - " + value;
        return name + "";
    }
}
