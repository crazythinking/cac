package net.engining.pcx.cc.process.service.common;

import java.math.BigDecimal;

import net.engining.pcx.cc.infrastructure.shared.model.ApGlBal;
import net.engining.pcx.cc.param.model.Subject;

import org.springframework.stereotype.Service;

/**
 * 总账计算逻辑组件
 * @author binarier
 *
 */
@Service
public class GlCalculator {

	public void writeOn(ApGlBal apGlBal, AcctingRecord acctingRecord, BigDecimal postAmount) {
		// 统计借贷记笔数
		switch (acctingRecord.getDbCrInd()) {
		case D:
			apGlBal.setDbCount(apGlBal.getDbCount() + 1);
			break;
		case C:
			apGlBal.setCrCount(apGlBal.getCrCount() + 1);
			break;
		default:
			break;
		}

		// 计算余额,发生额
		switch (acctingRecord.getSubject().balDbCrFlag) {
		case D: // 只允许借方余额
			switch (acctingRecord.getDbCrInd()) {
			case D: // 借
				// 借方发生额 =原借记发生额+借方金额
				apGlBal.setDbAmt(apGlBal.getDbAmt().add(postAmount));
				// 借方余额=原借方余额+借方金额
				apGlBal.setDbBal(apGlBal.getDbBal().add(postAmount));
				break;
			case C: // 贷
				// 贷方发生额=原贷记发生额+贷方金额
				apGlBal.setCrAmt(apGlBal.getCrAmt().add(postAmount));
				// 借方余额=原借方余额-贷方金额
				apGlBal.setDbBal(apGlBal.getDbBal().subtract(postAmount));
				break;
			default:
				throw new RuntimeException("枚举未定义");
			}

			break;
		case C: // 只允许贷方余额
			switch (acctingRecord.getDbCrInd()) {
			case D: // 借
				// 借方发生额 =原借记发生额+借方金额
				apGlBal.setDbAmt(apGlBal.getDbAmt().add(postAmount));
				// 贷方余额=原贷方余额-借方金额
				apGlBal.setCrBal(apGlBal.getCrBal().subtract(postAmount));
				break;
			case C: // 贷
				// 贷方发生额=原贷记发生额+贷方金额
				apGlBal.setCrAmt(apGlBal.getCrAmt().add(postAmount));
				// 贷方余额=原贷方余额+贷方金额
				apGlBal.setCrBal(apGlBal.getCrBal().add(postAmount));
				break;
			default:
				throw new RuntimeException("枚举未定义");
			}

			break;
		case B: // 按轧差金额
			// TODO 按轧差金额计算需要把一方置0

			switch (acctingRecord.getDbCrInd()) {
			case D: // 借
				// 借方发生额 =原借记发生额+借方金额
				apGlBal.setDbAmt(apGlBal.getDbAmt().add(postAmount));
				//
				BigDecimal tmpCrBal = apGlBal.getCrBal().subtract(postAmount);
				if (tmpCrBal.compareTo(BigDecimal.ZERO) > 0) {
					apGlBal.setCrBal(tmpCrBal);
				} else {
					apGlBal.setCrBal(BigDecimal.ZERO);
					apGlBal.setDbBal(apGlBal.getDbBal().add(tmpCrBal.negate()));
				}
				break;
			case C: // 贷
				// 贷方发生额=原贷记发生额+贷方金额
				apGlBal.setCrAmt(apGlBal.getCrAmt().add(postAmount));
				//
				BigDecimal tmpDbBal = apGlBal.getDbBal().subtract(postAmount);
				if (tmpDbBal.compareTo(BigDecimal.ZERO) > 0) {
					apGlBal.setDbBal(tmpDbBal);
				} else {
					apGlBal.setDbBal(BigDecimal.ZERO);
					apGlBal.setCrBal(apGlBal.getCrBal().add(tmpDbBal.negate()));
				}
				break;
			default:
				throw new RuntimeException("枚举未定义");
			}

			break;
		case T: // 双向余额
			switch (acctingRecord.getDbCrInd()) {
			case D: // 借
				// 借方发生额 =原借记发生额+借方金额
				apGlBal.setDbAmt(apGlBal.getDbAmt().add(postAmount));
				// 借方余额=原借方余额+借方金额
				apGlBal.setDbBal(apGlBal.getDbBal().add(postAmount));
				break;
			case C: // 贷
				// 贷方发生额=原贷记发生额+贷方金额
				apGlBal.setCrAmt(apGlBal.getCrAmt().add(postAmount));
				// 贷方余额=原贷方余额+贷方金额
				apGlBal.setCrBal(apGlBal.getCrBal().add(postAmount));
				break;
			default:
				throw new RuntimeException("枚举未定义");
			}

			break;
		default:
			break;
		}
	}

	public void writeOff(ApGlBal apGlBal, AcctingRecord acctingRecord, BigDecimal postAmount) {
		// 统计借贷记笔数
		switch (acctingRecord.getDbCrInd()) {
		case D:
			apGlBal.setDbCount(apGlBal.getDbCount() - 1);
			break;
		case C:
			apGlBal.setCrCount(apGlBal.getCrCount() - 1);
			break;
		default:
			break;
		}

		// 统计余额,发生额
		switch (acctingRecord.getSubject().balDbCrFlag) {
		case D: // 只允许借方余额
			switch (acctingRecord.getDbCrInd()) {
			case D: // 借
				// 借方发生额 =原借记发生额-借方金额
				apGlBal.setDbAmt(apGlBal.getDbAmt().subtract(postAmount));
				// 借方余额=原借方余额-借方金额
				apGlBal.setDbBal(apGlBal.getDbBal().subtract(postAmount));
				break;
			case C: // 贷
				// 贷方发生额=原贷记发生额-贷方金额
				apGlBal.setCrAmt(apGlBal.getCrAmt().subtract(postAmount));
				// 借方余额=原借方余额+贷方金额
				apGlBal.setDbBal(apGlBal.getDbBal().add(postAmount));
				break;
			default:
				throw new RuntimeException("枚举未定义");
			}

			break;
		case C: // 只允许贷方余额
			switch (acctingRecord.getDbCrInd()) {
			case D: // 借
				// 借方发生额 =原借记发生额-借方金额
				apGlBal.setDbAmt(apGlBal.getDbAmt().subtract(postAmount));
				// 贷方余额=原贷方余额+借方金额
				apGlBal.setCrBal(apGlBal.getCrBal().add(postAmount));
				break;
			case C: // 贷
				// 贷方发生额=原贷记发生额-贷方金额
				apGlBal.setCrAmt(apGlBal.getCrAmt().subtract(postAmount));
				// 贷方余额=原贷方余额-贷方金额
				apGlBal.setCrBal(apGlBal.getCrBal().subtract(postAmount));
				break;
			default:
				throw new RuntimeException("枚举未定义");
			}

			break;
		case B: // 按轧差金额
			// TODO 按轧差金额计算需要把一方置0

			switch (acctingRecord.getDbCrInd()) {
			case D: // 借
				// 借方发生额 =原借记发生额-借方金额
				apGlBal.setDbAmt(apGlBal.getDbAmt().subtract(postAmount));
				//
				BigDecimal tmpDbBal = apGlBal.getDbBal().subtract(postAmount);
				if (tmpDbBal.compareTo(BigDecimal.ZERO) > 0) {
					apGlBal.setDbBal(tmpDbBal);
				} else {
					apGlBal.setDbBal(BigDecimal.ZERO);
					apGlBal.setCrBal(apGlBal.getCrBal().add(tmpDbBal.negate()));
				}
				break;
			case C: // 贷
				// 贷方发生额=原贷记发生额-贷方金额
				apGlBal.setCrAmt(apGlBal.getCrAmt().subtract(postAmount));
				//
				BigDecimal tmpCrBal = apGlBal.getCrBal().subtract(postAmount);
				if (tmpCrBal.compareTo(BigDecimal.ZERO) > 0) {
					apGlBal.setCrBal(tmpCrBal);
				} else {
					apGlBal.setCrBal(BigDecimal.ZERO);
					apGlBal.setDbBal(apGlBal.getDbBal().add(tmpCrBal.negate()));
				}
				break;
			default:
				throw new RuntimeException("枚举未定义");
			}

			break;
		case T: // 双向余额
			switch (acctingRecord.getDbCrInd()) {
			case D: // 借
				// 借方发生额 =原借记发生额-借方金额
				apGlBal.setDbAmt(apGlBal.getDbAmt().subtract(postAmount));
				// 借方余额=原借方余额-借方金额
				apGlBal.setDbBal(apGlBal.getDbBal().subtract(postAmount));
				break;
			case C: // 贷
				// 贷方发生额=原贷记发生额-贷方金额
				apGlBal.setCrAmt(apGlBal.getCrAmt().subtract(postAmount));
				// 贷方余额=原贷方余额-贷方金额
				apGlBal.setCrBal(apGlBal.getCrBal().subtract(postAmount));
				break;
			default:
				throw new RuntimeException("枚举未定义");
			}

			break;
		default:
			break;
		}
	}
	
	public void reBalance(ApGlBal apgl, Subject subject){
		switch (subject.balDbCrFlag) {
		case D: // 只允许借方余额
			if (apgl.getDbBal() == null){
				apgl.setDbBal(BigDecimal.ZERO);
			}
			if (apgl.getCrBal() != null && apgl.getCrBal().compareTo(BigDecimal.ZERO) != 0){
				apgl.setDbBal(apgl.getDbBal().subtract(apgl.getCrBal()));
				apgl.setCrBal(BigDecimal.ZERO);
			}
			break;
		case C: // 只允许贷方余额
			if (apgl.getCrBal() == null){
				apgl.setCrBal(BigDecimal.ZERO);
			}
			if (apgl.getDbBal() != null && apgl.getDbBal().compareTo(BigDecimal.ZERO) != 0){
				apgl.setCrBal(apgl.getCrBal().subtract(apgl.getDbBal()));
				apgl.setDbBal(BigDecimal.ZERO);
			}

			break;
		case B: // 按轧差金额
			if (apgl.getCrBal() == null){
				apgl.setCrBal(BigDecimal.ZERO);
			}
			if (apgl.getDbBal() == null){
				apgl.setDbBal(BigDecimal.ZERO);
			}
			if (apgl.getDbBal().compareTo(apgl.getCrBal()) > 0 ){
				apgl.setDbBal(apgl.getDbBal().subtract(apgl.getCrBal()));
				apgl.setCrBal(BigDecimal.ZERO);
			}
			else{
				apgl.setCrBal(apgl.getCrBal().subtract(apgl.getDbBal()));
				apgl.setDbBal(BigDecimal.ZERO);
			}

			break;
		case T: // 双向余额
			// 不需要处理
			
			break;
		default:
			break;
		}
		
	}
}
