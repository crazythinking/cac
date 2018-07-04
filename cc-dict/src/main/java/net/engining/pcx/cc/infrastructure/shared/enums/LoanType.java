package net.engining.pcx.cc.infrastructure.shared.enums;

import net.engining.pg.support.meta.EnumInfo;

@EnumInfo({
	"R|消费转分期",
	"C|现金转分期",
	"B|账单分期",
	"P|POS分期",
	"M|大额分期（专项分期）"
})

public enum LoanType {
	/**
	 *	消费转分期 
	 */
	R("消费转分期"),
	/**
	 *	现金转分期 
	 */
	C("现金转分期"),
	/**
	 * 	账单分期
	 */
	B("账单分期"),
	/**
	 *	POS分期 
	 */
	P("POS分期"),
	/**
	 *	大额分期（专项分期） 
	 */
	M("大额分期（专项分期");

	private String description;

	private LoanType(String description){
		this.description = description;
	}

	public String getDescription() {
		return description;
	}
}
