package xmaslegacy;

import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.lazberry.xmaslegacy.ColorUtils;
import xmaslegacy.Utils.BookUtils;

public class Documents {


    public static @NotNull ItemStack IcingDocument() {
        return BookUtils.create(
                ColorUtils.chat("&4엘&k리&r&4ㅇ"),
                ColorUtils.chat("&b&l빙결 마법 관련 서적"),
                ColorUtils.chat(
                        """
                        &b&l빙결 관련 고대 서적
                
                        빙결마법은 생체를 뒤바꾸는
                        &4국가재앙&r수준의 고대 마법이다.
                        &m현재까지 치료 약물이 존재하지 않는다.&r
                        현재 '태양초'라는 특수 약물로만 회복 가능하다.
                        """),
                ColorUtils.chat(
                        """
                        &6&l관련 조항
                        
                        - 해당 마법은 &4국가 비상수준&r의
                        장악력과 파괴력을 사용하므로,
                        개인의 사용을 절대적으로 금한다.
                        
                        - 등급이 존재하지 않는다.
                        대신 재난사태 E급 발령이 허가된다.
                        """),
                ColorUtils.chat(
                        """
                        &7관련 문항&r
                        
                        기존 &4&k영원한 봄&r프로젝트에
                        사용되었던 마법이다. 추위를 응축시키기
                        위해 사용되었던 걸로 보인다.
                        
                        ⚠ 꼭 빙결 수치 확인하며
                        ⚠ 플레이 해주세요!
                        """
                ));
    }

    public static @NotNull ItemStack StolenBook() {
        return BookUtils.create(); //TODO
    }
}
