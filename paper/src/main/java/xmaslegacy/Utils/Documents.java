package xmaslegacy.Utils;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.ColorUtils;

public final class Documents {

	@ApiStatus.Internal
	private Documents() {
		throw new UnsupportedOperationException("Utility class");
	}

    public static @NotNull ItemStack IcingDocument() {
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

    public static @NotNull ItemStack StolenBook() {
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
}
