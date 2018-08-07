package net.engining.pcx.cc.batch.cc1800;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.joda.time.LocalDate;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import net.engining.pcx.cc.infrastructure.shared.enums.PostTxnTypeDef;
import net.engining.pcx.cc.infrastructure.shared.model.CactAccount;
import net.engining.pcx.cc.infrastructure.shared.model.CactAgeDue;
import net.engining.pcx.cc.param.model.Account;
import net.engining.pcx.cc.param.model.LatePaymentCharge;
import net.engining.pcx.cc.param.model.PostCode;
import net.engining.pcx.cc.param.model.enums.CalcBaseInd;
import net.engining.pcx.cc.param.model.enums.SysTxnCd;
import net.engining.pcx.cc.process.service.account.NewComputeService;
import net.engining.pcx.cc.process.service.account.NewPostService;
import net.engining.pcx.cc.process.service.account.PostDetail;
import net.engining.pcx.cc.process.service.common.BlockCodeUtils;
import net.engining.pcx.cc.process.service.common.UComputeDueAndAgeCode;
import net.engining.pg.parameter.ParameterFacility;

/**
 * 收取滞纳金
 * @author liyinxia
 *
 */
@Service
@StepScope
public class Cc1800P31GenerateLateChargeFee implements ItemProcessor<Cc1800IPostingInfo, Cc1800IPostingInfo> {
	
	/**
	 * 获取参数工具类
	 */
	@Autowired
	private ParameterFacility facility;
	
	/**
	 * 锁定码处理业务组件
	 */
	@Autowired
	private BlockCodeUtils blockCodeUtils;
	
	@Autowired
    private NewComputeService newComputeService;
	
	@Value("#{new java.util.Date(jobParameters['batchDate'].time)}")
	private Date batchDate;

	@Autowired
	private NewPostService newPostService;
	
	@Override
	public Cc1800IPostingInfo process(Cc1800IPostingInfo item) throws Exception {
		
//		Date lastProcessDate = systemStatus.lastProcessDate;
		
		for (List<Cc1800IAccountInfo> infos : item.getAccountList().values())
		{
			for (Cc1800IAccountInfo info : infos)
			{
				CactAccount cactAccount = info.getCactAccount();
				// 获取账户参数
				Account account = newComputeService.retrieveAccount(cactAccount);
				
				if (account.latePaymentCharge == null ||											// 没配滞纳金
					blockCodeUtils.getMergedLateFeeWaiveInd(cactAccount.getBlockCode(), account)||	// 检查账户锁定码免除滞纳金标志
					cactAccount.getWaiveLatefeeInd()||												// 检查账户滞纳金免除标示
					cactAccount.getPmtDueDate() == null)											// 未经过首次账单日，直接返回
				{
					continue;
				}
				
				// 滞纳金参数
				LatePaymentCharge latePayCharge = account.latePaymentCharge;
				// 增加宽限天数后的滞纳金收取日
				Date assessDay = null;
				switch (latePayCharge.latePaymentChargeDate)
				{
					case C : assessDay = cactAccount.getInterestDate(); break;
					case D : assessDay = cactAccount.getPmtDueDate(); break;
					case G : assessDay = cactAccount.getGraceDate(); break;
					case P : assessDay = cactAccount.getFirstOverdueDate();break;
					default : throw new IllegalArgumentException("should not be here");
				}
				
				if (batchDate.compareTo( assessDay ) != 0 )
				{
					continue;
				}
				
				// 账户的账龄<滞纳金参数表中的最小账龄或者是溢缴款，则不收取滞纳金
				if (cactAccount.getAgeCd().compareTo(latePayCharge.minAgeCd) < 0
						|| cactAccount.getAgeCd() == UComputeDueAndAgeCode.OVERPMT_AGE_CD)
				{
					continue;
				}
				// 最小还款额<=零，则不收取滞纳金
				if (cactAccount.getTotDueAmt().compareTo(BigDecimal.ZERO) < 0)
				{
					continue;		
				}
				
				// 滞纳金基准金额
				BigDecimal latePayAmt = BigDecimal.ZERO;
				// 滞纳金计算基准金额指示： 
				CalcBaseInd calcBaseInd = latePayCharge.calcBaseInd;
				// T - 用总的最小还款额剩余部分（total due）
				if (CalcBaseInd.T.equals(calcBaseInd)) {
					latePayAmt = cactAccount.getTotDueAmt();
				}
				// L - 用往期最小还款额剩余部分（last due）
				else if (CalcBaseInd.L.equals(calcBaseInd)) {
					Iterator<CactAgeDue> iter = info.getCactAgeDues().iterator();
					while (iter.hasNext())
					{
						CactAgeDue obj = iter.next();
						if (iter.hasNext())
						{
							latePayAmt = latePayAmt.add(obj.getAgeDueAmt());
						}
					}
//					latePayAmt = cactAccount.getAge1DueAmt()
//							.add(cactAccount.getAge2DueAmt())
//							.add(cactAccount.getAge3DueAmt())
//							.add(cactAccount.getAge4DueAmt())
//							.add(cactAccount.getAge5DueAmt())
//							.add(cactAccount.getAge6DueAmt())
//							.add(cactAccount.getAge7DueAmt())
//							.add(cactAccount.getAge8DueAmt())
//							.add(cactAccount.getAge9DueAmt());
				}
				// C - 用当期最小还款额剩余部分（ctd due）
				else if (CalcBaseInd.C.equals(calcBaseInd)) {
					int index = -1;
					if (info.getCactAgeDues().size() > 0) {
						index = info.getCactAgeDues().size() - 1;
					}
					if (index != -1) {
						latePayAmt = info.getCactAgeDues().get(index).getAgeDueAmt();
					}			
				}
				
				// 滞纳金基准金额小于等于免收滞纳金阈值，直接返回
				if (latePayCharge.threshold != null && latePayAmt.compareTo(latePayCharge.threshold) <= 0)
				{
					continue;
				}
						
				// 如果滞纳金基准金额大于零，则调用交易生成和入账逻辑
				if (latePayAmt.compareTo(BigDecimal.ZERO) > 0)
				{
					// 生成一笔滞纳金收取交易，并入账
					// 查找系统内部交易类型对照表
					//		SysTxnCdMapping sysTxnCd = facility.getParameter(SysTxnCdMapping.class, String.valueOf(SysTxnCd.S05) );
							// 根据交易码，查找交易码对象
					//		PostCode txnCode = facility.getParameter(PostCode.class, sysTxnCd.postCode);
							// 根据产品代码，查找超限参数对象
							
					PostCode postCode = facility.getParameter(PostCode.class, account.sysTxnCdMapping.get(SysTxnCd.S05));

					// 滞纳金参数
							
					// 计算滞纳金
					BigDecimal latePayFee = newComputeService.calcTieredAmount(latePayCharge.tierInd, latePayCharge.chargeRates, latePayAmt, latePayAmt).setScale(2, BigDecimal.ROUND_HALF_UP);
					// 最小收费金额
					if (latePayFee.compareTo(latePayCharge.minCharge) < 0)
					{
						latePayFee = latePayCharge.minCharge;
					}
					// 最大收费金额
					if (latePayFee.compareTo(latePayCharge.maxCharge) > 0)
					{
						latePayFee = latePayCharge.maxCharge;
					}
							// 本年最大收费金额
					//		if (latePayFee.compareTo(latePayCharge.yearMaxCharge.subtract(item.getYtdLpcAmt())) > 0) {
					//			latePayFee = latePayCharge.yearMaxCharge.subtract(item.getYtdLpcAmt());
					//		}
							// 本年最大收取笔数
					//		if (latePayCharge.yearMaxCnt - item.getYtdLpcCnt() <= 0) {
					//			latePayFee = BigDecimal.ZERO;
					//		}
					
					if (latePayFee.compareTo(BigDecimal.ZERO) >= 0)
					{
						// 生成内部交易
						PostDetail detail = new PostDetail();
							
						detail.setTxnDate(batchDate); // 交易日期
						detail.setTxnTime(new Date()); // 交易时间
						detail.setPostTxnType(PostTxnTypeDef.M); // 入账交易类型
						detail.setPostCode(postCode.postCode); // 交易码
						detail.setTxnAmt(latePayFee); // 交易金额
						detail.setPostAmt(latePayFee); // 入账币种金额
						detail.setTxnCurrCd(cactAccount.getCurrCd()); // 交易币种代码
						detail.setPostCurrCd(cactAccount.getCurrCd()); // 入账币种代码
						
						newPostService.postToAccount(info.getAcctModel(), new LocalDate(batchDate), detail, true, 0);
					}		
					// 本年滞纳金收取金额
//					cactAccount.setYtdLpcAmt(txnPost.getPostAmt().add(cactAccount.getYtdLpcAmt()));
					// 本年滞纳金收取笔数
//					cactAccount.setYtdLpcCnt(cactAccount.getYtdLpcCnt() + 1);
				}
			}
		}

		return item;
	}
}
