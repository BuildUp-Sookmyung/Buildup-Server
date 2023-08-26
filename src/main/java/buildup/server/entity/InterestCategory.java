package buildup.server.entity;


public enum InterestCategory {
    MANAGEMENT_BUSINESS("경영/사무"),
    ACCOUNTING("회계/재무/세무"),
    MARKETING("홍보/광고마케팅"),
    SALES("영업"),
    PRODUCTION("생산/제조"),
    CONSTRUCTION("건설/건축"),
    COMMERCE("무역/유통"),
    PUBLIC("공공/복지"),
    IT("IT개발/데이터"),
    DESIGN("디자인"),
    FINANCE("금융"),
    MEDICINE("의료/보건"),
    EDUCATION("교육"),
    SPECIALIST("전문/특수직"),
    ENTERTAINMENT("미디어/문화/스포츠"),
    STRATEGY("기획/전략"),
    RESEARCH("연구/R&D"),
    HRD("인사/노무/HRD"),
    MERCHANDISING("상품기획/MD"),
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