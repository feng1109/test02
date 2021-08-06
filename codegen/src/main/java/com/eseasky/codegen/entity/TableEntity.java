package com.eseasky.codegen.entity;

import java.util.List;

public class TableEntity {
	/**
	 * 名称
	 */
	private String tableName;
	/**
	 * 备注
	 */
	private String comments;
	/**
	 * 主键
	 */
	private ColumnEntity pk;
	/**
	 * 列名
	 */
	private List<ColumnEntity> columns;
	/**
	 * 驼峰类型
	 */
	private String caseClassName;
	/**
	 * 普通类型
	 */
	private String lowerClassName;

	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public String getComments() {
		return comments;
	}

	public void setComments(String comments) {
		this.comments = comments;
	}

	public  ColumnEntity getPk() {
		return pk;
	}

	public void setPk(ColumnEntity pk) {
		this.pk = pk;
	}

	public List<ColumnEntity> getColumns() {
		return columns;
	}

	public void setColumns(List<ColumnEntity> columns) {
		this.columns = columns;
	}

	public String getCaseClassName() {
		return caseClassName;
	}

	public void setCaseClassName(String caseClassName) {
		this.caseClassName = caseClassName;
	}

	public String getLowerClassName() {
		return lowerClassName;
	}

	public void setLowerClassName(String lowerClassName) {
		this.lowerClassName = lowerClassName;
	}
}
