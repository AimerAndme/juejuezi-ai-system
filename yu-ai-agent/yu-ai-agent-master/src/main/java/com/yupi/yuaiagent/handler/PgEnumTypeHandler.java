//package com.yupi.yuaiagent.handler;
//
//import org.apache.ibatis.type.BaseTypeHandler;
//import org.apache.ibatis.type.JdbcType;
//import org.apache.ibatis.type.MappedJdbcTypes;
//import org.apache.ibatis.type.MappedTypes;
//
//
//import java.sql.*;
//
///**
// * 通用 PG 枚举类型处理器（适配所有自定义枚举）
// * 要求枚举类必须有 fromCode(String) 方法和 getCode() 方法
// */
//@MappedJdbcTypes(JdbcType.OTHER) // PG 枚举在 JDBC 中类型为 OTHER
//public class PgEnumTypeHandler<E extends Enum<E>> extends BaseTypeHandler<E> {
//
//    private final Class<E> type;
//
//    // 构造函数（MyBatis 自动注入枚举类型）
//    public PgEnumTypeHandler(Class<E> type) {
//        this.type = type;
//    }
//
//    @Override
//    public void setNonNullParameter(PreparedStatement ps, int i, E parameter, JdbcType jdbcType) throws SQLException {
//        PGobject pgObject = new PGobject();
//        pgObject.setType(type.getSimpleName().toLowerCase() + "_enum"); // 对应 PG 枚举类型名（如 sender_type_enum）
//        pgObject.setValue(parameter.getClass().getMethod("getCode").invoke(parameter).toString());
//        ps.setObject(i, pgObject);
//    }
//
//    @Override
//    public E getNullableResult(ResultSet rs, String columnName) throws SQLException {
//        String code = rs.getString(columnName);
//        return code == null ? null : getEnumByCode(code);
//    }
//
//    @Override
//    public E getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
//        String code = rs.getString(columnIndex);
//        return code == null ? null : getEnumByCode(code);
//    }
//
//    @Override
//    public E getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
//        String code = cs.getString(columnIndex);
//        return code == null ? null : getEnumByCode(code);
//    }
//
//    // 反射调用枚举的 fromCode 方法解析
//    private E getEnumByCode(String code) {
//        try {
//            return (E) type.getMethod("fromCode", String.class).invoke(null, code);
//        } catch (Exception e) {
//            throw new RuntimeException("解析 PG 枚举失败：" + code, e);
//        }
//    }
//}