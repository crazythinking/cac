package net.engining.pcx.cc.process.service.common;

import com.querydsl.jpa.impl.JPAQueryFactory;
import net.engining.gm.facility.SystemStatusFacility;
import net.engining.gm.infrastructure.enums.SystemStatusType;
import net.engining.gm.infrastructure.enums.TxnDirection;
import net.engining.pcx.cc.infrastructure.shared.enums.PostingFlag4InternalAcct;
import net.engining.pcx.cc.infrastructure.shared.enums.TxnDetailType;
import net.engining.pcx.cc.infrastructure.shared.model.CactInternalAcct;
import net.engining.pcx.cc.infrastructure.shared.model.CactInternalTxnPostHst;
import net.engining.pcx.cc.infrastructure.shared.model.CactIntrnlTxnPostOl;
import net.engining.pcx.cc.infrastructure.shared.model.QCactIntrnlTxnPostOl;
import net.engining.pcx.cc.param.model.InternalAccount;
import net.engining.pcx.cc.param.model.Subject;
import net.engining.pcx.cc.param.model.enums.InternalAccountStatus;
import net.engining.pcx.cc.param.model.enums.RedBlueInd;
import net.engining.pcx.cc.process.service.support.Provider7x24;
import net.engining.pg.parameter.ParameterFacility;
import net.engining.pg.support.core.context.OrganizationContextHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;


@Service
public class InternalTxnPostTask {

	private Logger logger = LoggerFactory.getLogger(getClass());

	@PersistenceContext
	private EntityManager em;

	@Autowired
	private Provider7x24 provider7x24;

	@Autowired
	private SystemStatusFacility systemStatusFacility;
	
	@Autowired
	private ParameterFacility parameterCacheFacility;

	@Transactional
	public void internalTxnPost() {
		OrganizationContextHolder.setCurrentOrganizationId("*");

		logger.debug("进行联机内部记账记录轮询");
		//联机时取businessDate过滤，批量时取processDate过滤
		Date selectDate =systemStatusFacility.getSystemStatus().businessDate ;
		if ( systemStatusFacility.getNowSystemStatus().equals(SystemStatusType.B)){
			selectDate = systemStatusFacility.getSystemStatus().processDate;
		}  
			
		QCactIntrnlTxnPostOl qCactIntrnlTxnPostOl = QCactIntrnlTxnPostOl.cactIntrnlTxnPostOl;
		
		List<CactIntrnlTxnPostOl> poll = new JPAQueryFactory(em)
				.select(qCactIntrnlTxnPostOl)
				.from(qCactIntrnlTxnPostOl)
			.where(qCactIntrnlTxnPostOl.postingFlag.eq(PostingFlag4InternalAcct.FFF)
					.and(qCactIntrnlTxnPostOl.postDate.eq(selectDate)))
			.orderBy(qCactIntrnlTxnPostOl.postDate.asc())
			.fetch();

		if (!poll.isEmpty()) {
			logger.debug("准备处理{}条入账记录", poll.size());
		 
			for (CactIntrnlTxnPostOl detail : poll) {
				//查内部账户
				CactInternalAcct cactInternalAcct = em.find(CactInternalAcct.class,detail.getInternalAcctId());
				
				if(cactInternalAcct == null){
					InternalAccount internalAccount = parameterCacheFacility.getParameter(InternalAccount.class, detail.getInternalAcctId());
					cactInternalAcct = new CactInternalAcct();
					cactInternalAcct.setCrBal(BigDecimal.ZERO);
					cactInternalAcct.setDbBal(BigDecimal.ZERO);
					cactInternalAcct.setLastCrBal(BigDecimal.ZERO);
					cactInternalAcct.setLastDbBal(BigDecimal.ZERO);
					cactInternalAcct.setInternalAcctId(detail.getInternalAcctId());
					cactInternalAcct.setSetupDate(systemStatusFacility.getSystemStatus().businessDate);
					cactInternalAcct.setSubjectCd(internalAccount.subjectCd);
					cactInternalAcct.setInternalAcctName(internalAccount.desc);
					cactInternalAcct.setLastUpdateDate(new Date());
					cactInternalAcct.setBizDate(provider7x24.getCurrentDate().toDate());
					cactInternalAcct.fillDefaultValues();
					em.persist(cactInternalAcct);
				}
				//查内部账户对应参数
				InternalAccount internalAccount = parameterCacheFacility.getParameter(InternalAccount.class, detail.getInternalAcctId());
				
				Subject subject = parameterCacheFacility.getParameter(Subject.class, internalAccount.subjectCd);
				
				//内部账户状态必须是open才能入账
				if(internalAccount.status.equals(InternalAccountStatus.OPEN)){
					
					caseInternalTxnPost(subject ,detail.getDbCrInd(),detail.getPostAmt(),detail.getRedBlueInd(),cactInternalAcct);
				 
					detail.setPostingFlag(PostingFlag4InternalAcct.F00);
					
					
				}else if(internalAccount.status.equals(InternalAccountStatus.CLOSE)){
					
					detail.setPostingFlag(PostingFlag4InternalAcct.F01);
					
				}else if(internalAccount.status.equals(InternalAccountStatus.DESTROY)){
					
					detail.setPostingFlag(PostingFlag4InternalAcct.F02);
					
				}
				
				CactInternalTxnPostHst  hst = new CactInternalTxnPostHst();
				hst.setDbCrInd(detail.getDbCrInd());
				hst.setInternalAcctId(detail.getInternalAcctId());
				hst.setInternalAcctPostCode(detail.getInternalAcctPostCode());
				hst.setTxnDetailSeq(detail.getTxnDetailSeq());
				hst.setTxnDetailType(detail.getTxnDetailType());
				hst.setOrg(detail.getOrg());
				hst.setPostAmt(detail.getPostAmt());
				hst.setPostCurrCd(detail.getPostCurrCd());
				hst.setPostDate(detail.getPostDate());
				hst.setDbCrInd(detail.getDbCrInd());
				hst.setPostingFlag(detail.getPostingFlag());
				hst.setRedBlueInd(detail.getRedBlueInd());
				hst.setTxnPostSeq(detail.getTxnSeq());
				hst.setTxnPostType(TxnDetailType.L);
				hst.setLastUpdateDate(new Date());
				hst.setBizDate(provider7x24.getCurrentDate().toDate());
				hst.fillDefaultValues();
				em.persist(hst);
				em.remove(detail);
				 
			}
			 
		}
		else {
			logger.debug("无内部户入帐交易需要处理");	
		}
		 
	}
	
	/**
	 * 内部账交易入账处理，主要是对当日借方余额，贷方余额进行记账
	 * @param subject 		内部帐对应的会计科目号
	 * @param direction		交易的金额方向，D|借方", "C|贷方", "O|其他(查询等)"
	 * @param postAmt
	 * @param redBlueInd	红蓝标志
	 * @param cactInternalAcct
	 */
	public void caseInternalTxnPost(Subject subject , TxnDirection direction, BigDecimal postAmt ,RedBlueInd redBlueInd , CactInternalAcct cactInternalAcct){
			
			switch (redBlueInd) {
			case R: // 红字(撤销交易)
				 writeOff(subject,direction,postAmt, cactInternalAcct);
				break;
			case N: // 正常交易
				 writeOn(subject, direction, postAmt, cactInternalAcct);
				break;
			case B: // 蓝字（正常交易）
				writeOn(subject, direction, postAmt, cactInternalAcct);
				break;
			default:
				throw new RuntimeException("枚举未定义");
			}
			
	}
	
	/**
	 * 记账
	 * @param subject
	 * @param direction
	 * @param postAmt
	 * @param cactInternalAcct
	 */
	public void writeOn(Subject subject , TxnDirection direction, BigDecimal postAmt , CactInternalAcct cactInternalAcct){
		// 计算余额,发生额；根据余额允许的记账方向，进行处理
		switch (subject.balDbCrFlag) {
		case D: // 只允许记借方余额
			switch (direction) {
			case D: // 借
				// 借方余额=原借方余额+交易金额
				cactInternalAcct.setDbBal(cactInternalAcct.getDbBal().add(postAmt));
				break;
			case C: // 贷
				// 借方余额=原借方余额-交易金额
				cactInternalAcct.setDbBal(cactInternalAcct.getDbBal().subtract(postAmt));
				break;
			default:
				throw new RuntimeException("枚举未定义");
			}

			break;
		case C: // 只允许记贷方余额
			switch (direction) {
			case D: // 借
				// 贷方余额=原贷方余额-交易金额
				cactInternalAcct.setCrBal(cactInternalAcct.getCrBal().subtract(postAmt));
				break;
			case C: // 贷
				// 贷方余额=原贷方余额+交易金额
				cactInternalAcct.setCrBal(cactInternalAcct.getCrBal().add(postAmt));
				break;
			default:
				throw new RuntimeException("枚举未定义");
			}

			break;
		case B: // 按轧差金额记账
			switch (direction) {
			case D: // 借
				//贷方余额与交易金额的差
				BigDecimal tmpCrBal = cactInternalAcct.getCrBal().subtract(postAmt);
				//差大于0，贷方余额=差额，借方余额不变
				if (tmpCrBal.compareTo(BigDecimal.ZERO) > 0) {
					cactInternalAcct.setCrBal(tmpCrBal);
				} 
				else {//差小于等于0，贷方余额=0，借方余额=原借方余额+(-差额)
					cactInternalAcct.setCrBal(BigDecimal.ZERO);
					cactInternalAcct.setDbBal(cactInternalAcct.getDbBal().add(tmpCrBal.negate()));
				}
				break;
			case C: // 贷
				//借方余额与交易金额的差额
				BigDecimal tmpDbBal = cactInternalAcct.getDbBal().subtract(postAmt);
				//差大于0，借方余额=差额，贷方余额不变
				if (tmpDbBal.compareTo(BigDecimal.ZERO) > 0) {
					cactInternalAcct.setDbBal(tmpDbBal);
				} 
				else {//差小于等于0，借方余额=0，贷方余额=原贷方余额+(-差额)
					cactInternalAcct.setDbBal(BigDecimal.ZERO);
					cactInternalAcct.setCrBal(cactInternalAcct.getCrBal().add(tmpDbBal.negate()));
				}
				break;
			default:
				throw new RuntimeException("枚举未定义");
			}

			break;
		case T: // 双向余额
			switch (direction) {
			case D: // 借
				// 借方余额=原借方余额+交易金额
				cactInternalAcct.setDbBal(cactInternalAcct.getDbBal().add(postAmt));
				break;
			case C: // 贷
				// 贷方余额=原贷方余额+交易金额
				cactInternalAcct.setCrBal(cactInternalAcct.getCrBal().add(postAmt));
				break;
			default:
				throw new RuntimeException("枚举未定义");
			}

			break;
		default:
			break;
		}
			
	}
	
	/**
	 * 勾销，针对记账进行反向交易
	 * @param subject
	 * @param direction
	 * @param postAmt
	 * @param cactInternalAcct
	 */
	public void writeOff(Subject subject ,  TxnDirection direction, BigDecimal postAmt , CactInternalAcct cactInternalAcct) {
		// 计算余额,发生额；根据余额允许的记账方向，进行处理
		switch (subject.balDbCrFlag) {
		case D: // 只允许记借方余额
			switch (direction) {
			case D: // 借
				// 借方余额=原借方余额-交易金额
				cactInternalAcct.setDbBal(cactInternalAcct.getDbBal().subtract(postAmt));
				break;
			case C: // 贷
				// 借方余额=原借方余额+交易金额
				cactInternalAcct.setDbBal(cactInternalAcct.getDbBal().add(postAmt));
				break;
			default:
				throw new RuntimeException("枚举未定义");
			}

			break;
		case C: // 只允许记贷方余额
			switch (direction) {
			case D: // 借
				// 贷方余额=原贷方余额+交易金额
				cactInternalAcct.setCrBal(cactInternalAcct.getCrBal().add(postAmt));
				break;
			case C: // 贷
				// 贷方余额=原贷方余额-交易金额
				cactInternalAcct.setCrBal(cactInternalAcct.getCrBal().subtract(postAmt));
				break;
			default:
				throw new RuntimeException("枚举未定义");
			}

			break;
		case B: // 按轧差金额记账
			switch (direction) {
			case D: // 借
				//借方余额与交易金额的差
				BigDecimal tmpDbBal = cactInternalAcct.getDbBal().subtract(postAmt);
				//差额大于0，借方余额=差额
				if (tmpDbBal.compareTo(BigDecimal.ZERO) > 0) {
					cactInternalAcct.setDbBal(tmpDbBal);
				} 
				else {//差额小于等于0，借方余额=0，贷方余额=原贷方余额+(-差额)
					cactInternalAcct.setDbBal(BigDecimal.ZERO);
					cactInternalAcct.setCrBal(cactInternalAcct.getCrBal().add(tmpDbBal.negate()));
				}
				break;
			case C: // 贷
				//贷方余额与交易金额的差
				BigDecimal tmpCrBal = cactInternalAcct.getCrBal().subtract(postAmt);
				//差额大于0，贷方余额=差额
				if (tmpCrBal.compareTo(BigDecimal.ZERO) > 0) {
					cactInternalAcct.setCrBal(tmpCrBal);
				} 
				else {//差额小于等于0，贷方余额=0，借方余额=原借方余额+(-差额)
					cactInternalAcct.setCrBal(BigDecimal.ZERO);
					cactInternalAcct.setDbBal(cactInternalAcct.getDbBal().add(tmpCrBal.negate()));
				}
				break;
			default:
				throw new RuntimeException("枚举未定义");
			}

			break;
		case T: // 双向余额
			switch (direction) {
			case D: // 借
				// 借方余额=原借方余额-交易金额
				cactInternalAcct.setDbBal(cactInternalAcct.getDbBal().subtract(postAmt));
				break;
			case C: // 贷
				// 贷方余额=原贷方余额-交易金额
				cactInternalAcct.setCrBal(cactInternalAcct.getCrBal().subtract(postAmt));
				break;
			default:
				throw new RuntimeException("枚举未定义");
			}

			break;
		default:
			break;
		}
	}
	 
}
