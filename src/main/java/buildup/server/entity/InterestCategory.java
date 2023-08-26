package buildup.server.entity;


public enum InterestCategory {
    MANAGEMENT_BUSINESS("경영/사무"),
    IT("IT개발/데이터"),
    MARKETING("홍보/광고마케팅"),
    SALES("영업"),
    DESIGN("디자인"),
    EDUCATION("교육"),
    ENTERTAINMENT("미디어/문화/스포츠"),
    ENGINEERING("엔지니어링/설계");

    private final String field;

    InterestCategory(String field) {
        this.field = field;
    }

    public String getField() {
        return this.field;
    }

    public static InterestCategory fromField(String field) {
        for (InterestCategory category : InterestCategory.values()) {
            if (category.getField().equals(field)) {
                return category;
            }
        }
        throw new IllegalArgumentException("Invalid field: " + field);
    }
    }