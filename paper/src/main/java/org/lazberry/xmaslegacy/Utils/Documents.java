package org.lazberry.xmaslegacy.Utils;

import lombok.experimental.UtilityClass;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.ColorUtils;

@UtilityClass
public final class Documents {

    public @NotNull ItemStack IcingDocument() {
        return BookUtils.create(
                ColorUtils.chat("&4엘&k리&r&4#"),
                ColorUtils.chat("&b&l빙결 마법 관련 서적"),
                ColorUtils.chat(
                        """
                        &b&l빙결&0 관련 고대 서적
                
                        빙결마법은 생체를 뒤바꾸는
                        &4국가재앙&r수준의 고대 마법이다.
                        &m현재까지 치료 약물이 존재하지 않는다.&r
                        현재 '태양초'라는 특수 약물로만 회복 가능하다.
                        """),
                ColorUtils.chat(
                        """
                        &6&l관련 조항&r&0
                        
                        - 해당 마법은 &4국가 비상수준&0의
                        장악력과 파괴력을 사용하므로,
                        개인의 사용을 절대적으로 금한다.
                        
                        - 등급이 존재하지 않는다.
                        대신 재난사태 E급 발령이 허가된다.
                        """),
                ColorUtils.chat(
                        """
                        &7관련 문항&0
                        
                        기존 &4&k영원한 봄&r&0프로젝트에
                        사용되었던 마법이다. 추위를 응축시키기
                        위해 사용되었던 걸로 보인다.
                        
                        ⚠ 꼭 빙결 수치 확인하며
                        ⚠ 플레이 해주세요!
                        """
                ));
    }

    public @NotNull ItemStack StolenBook() {
        return BookUtils.create(
				ColorUtils.chat("&5엘리스"),
		        ColorUtils.chat("&b백야&f에 관하여"),
		        ColorUtils.chat( //엘리안에게 바칩니다.
				        """
				        이 책을 &b&k###&r&0에게 바칩니다..
				        
				        이 기록은 &4### #&r&0프로젝트의
				        기록을 위해 작성되었습니다.
				        &61급 기밀&0자료이며
				        외부에서의 수정 혹은 열람을
				        법적으로 금지합니다.
				        """),
		        ColorUtils.chat(
				        """
				        &6발단&0
				        
				        &4&k엘리안&r&0이 프로젝트를 시작하게 된
				        계기이다. 그녀는 이미 이 행성의 에너지가
				        모두 고갈되었음을 발견했다.
				        그 즉시 상부에 보고하며 2광년 거리의
				        행성으로의 이주와 테라포밍을 위한
				        자금을 요청했다.
				        """),
				ColorUtils.chat(
						"""
						하지만 상부는 먹고 놀기에 바빴다.
						아무도 &4&k엘리안&r&0의 말을 듣지 않았고, 상부 비서를
						아버지로 둔 나의 호소조차 먹히지
						않았다.그때였다. &4&k엘리안&r&0이 나에게
						이상한 프로젝트를 제안한 시점이.
						"""),
				ColorUtils.chat(
						"""
						비상 해결 프로토컬, &4영원&k# #&r&0
						
						이주와 테라포밍 자금모금에 실패하자
						그녀는 도망치기보다 행성을 고치기로 했다.
						그렇게 시작되었다. 재앙이.
						
						(..이후 내용은 찢어져 보이지 않는다.)
						""")
        );
    }

	//Finale book
	public @NotNull ItemStack ElianDiary(boolean original) {
		String name = original ? "&c엘리안&0" : "&c&k###&0&r";
		return BookUtils.create(
				ColorUtils.chat(name),
				ColorUtils.chat(name + "&f의 일지"),
				ColorUtils.chat(
						"""
						&bIAG -20Y&0
						
						평소와 같이 마력측정기를 동원해
						기후 상태를 확인중이었다.그때였다.
						뭔가 이상한걸 발견해버렸다.
						발견해서는 안될거 같은 &c무언가&0를.
						
						난 즉시 조사에 들어갔다.
						"""),
				ColorUtils.chat(
						"""
						&bIAG -18Y&0
						
						알아냈다.
						
						근데, 알아내서는 안될거 같았다.
						
						&4말도 안되는 힘의 빙결마법을.&0
						
						너무나도 이상했다.
						"""),
				ColorUtils.chat(
						"""
						
						분명 빙결마법은 얼음 만들때나
						쓰는 &7D급 하위마법&0이다.
						방금 마법을 배운 사람조차 쉽게
						시전할 수 있는. 근데 모든 마법은
						사용자에 마력에 따라가기 나름이다.
						
						인간이 한건지 조차 의심되는,
						엄청난 양의 마력이었다.
						나조차 겁이날 정도로.
						
						"""),
				ColorUtils.chat(
						"""
						&bIAG -15Y&0
						
						난 마력의 주인이 너무도
						궁금해졌다. 하지만 시간이 없었다.
						조금만 지체되었다간 행성 종말급
						빙하기가 찾아올 것이다. 난 이 내용을
						바로 상부에 보고했다. 지원을 받아
						마력 방파제를 막을 계획이었다.
						하지만, 매몰차게 거절당했다.
						그런 마력은 상부 특수마력부대에서나
						쓰인다고.
						"""),
				ColorUtils.chat(
						"""
						&bIAG -10Y&0
						
						결국에 난 나만의 프로젝트를
						하기로 했다.
						
						&4"영원한 봄"&0 프로젝트를.
						
						내 마력과 나라 최고 기술력을 이용하여
						빙결마법을 전부 자연에너지로 돌려버리는
						일명 에너지 재활용 장치였다.
						
						"""),
				ColorUtils.chat(
						"""
						&bIAG -7Y&0
						
						폭주한 빙결마법이 나라에
						도착하기까지 예상시간 일주일.
						
						난 도서관에서 이상한 책을 발견했다.
						금지도서 구역에서 말이다.
						그중 한 문구가 눈에 띄였다.
						
						&o&4"초대 빙결마법 : 시전자의 수에 따라 마력이 제곱수로 폭주한다."
						&o&4"고대 아이스에이지 이전 시절부터 모든 마법사들에게 금지되던 마법이다."
						"""),
				ColorUtils.chat(
						"""
						이 일의 배후를 찾아야겠다고 생각했다.
						단순한 자연재해가 아닌, 누군가가 이 일을
						꾸몄다는 생각이 뇌리를 스쳐갔다.
						
						이 나라는 상부 귀족들이 자원이 80%를 회수해가는
						악질정인 정책이 있다. 그래서 과학자, 마법사, 상부 귀족
						을 제외한 나머지 백성들은 항상 굶주려있다.
						
						최근에 들은 소문이 있다.
						&5"혁명군"
						"""),
				ColorUtils.chat(
						"""
						바로 혁명군 근처 주둔지에서
						마력을 직접 측정해보았다. 역시나,
						국가 전쟁에서조차 쓰이지 않을 양의
						엄청난 마력이 측정되었다.
						
						굶주리던 백성들의 발악이었던 것이다.
						난 장치 완성을 서둘렀다.
						나로썬 혁명군의 마법부터 막는게
						중요했다.
						"""),
				ColorUtils.chat(
						"""
						
						
						(빈 페이지)
						
						
						""")
		);
	}
}
