package org.lazberry.xmaslegacy.enchant.material;

import io.th0rgal.oraxen.api.OraxenItems;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.inventory.ItemRarity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.lazberry.xmaslegacy.XmasLegacy;
import org.lazberry.xmaslegacy.utils.BookUtils;
import org.lazberry.xmaslegacy.utils.ColorUtils;
import org.lazberry.xmaslegacy.utils.ItemBuilder;
import org.lazberry.xmaslegacy.utils.KeyUtils;

public class EnchantMaterial {
	public static final NamespacedKey key = KeyUtils.get("enchant_material");

    private EnchantMaterial() {}

	public static ItemStack DowngradeProtector() {
		var builder = OraxenItems.getItemById("downgrade_protector");
		ItemStack item = builder == null ? new ItemStack(Material.AMETHYST_SHARD) : builder.build();

		return ItemBuilder.of(XmasLegacy.getInstance(), item)
				.name(ColorUtils.chat("&d&l등급 하락 억제기"))
				.lore(
						ColorUtils.chat("&7강화 중 등급 하락 시 하락을 취소하며,"),
						ColorUtils.chat("&7억제기는 소멸합니다.(일회성)")
				)
				.setGlint(true)
				.setMaxStackSize(16)
				.setTag(key, PersistentDataType.STRING, "downgrade_protector")
				.hideAllFlags()
				.build();
	}

	public static ItemStack BreakProtector() {
		var builder = OraxenItems.getItemById("break_protector");
		ItemStack item = builder == null ? new ItemStack(Material.BARRIER) : builder.build();

		return ItemBuilder.of(XmasLegacy.getInstance(), item)
				.name(ColorUtils.chat("&6&l부활의 표식"))
				.lore(
						ColorUtils.chat("&7강화 중 아이템 파괴시 해당 파괴를 취소하며,"),
						ColorUtils.chat("&7해당 표식은 소멸합니다.(일회성)")
				)
				.setGlint(true)
				.hideAllFlags()
				.setMaxStackSize(1)
				.setTag(key, PersistentDataType.STRING, "break_protector")
				.build();
	}

	public static ItemStack InfoBook() {
		ItemStack book = ItemBuilder.of(XmasLegacy.getInstance(), Material.WRITTEN_BOOK)
				.name(ColorUtils.chat("&e&l강화 설명서"))
				.lore(
						ColorUtils.chat("&7강화, 필요재료, 혹은 \"&#C822FF무&#9638F8기 &#3264E9초&#007AE1월\"&7에 관한 자료입니다."),
						ColorUtils.chat("&7해당 문서에 결제 약관, 규칙, 혹은 배상 조건이 모두 기입되어 있으므로"),
						ColorUtils.chat("&7미필독으로 인한 피해는 배상하지 않습니다.")
				)
				.setMaxStackSize(1)
				.setTag(key, PersistentDataType.STRING, "info_book")
				.hideAllFlags()
				.build();

		return BookUtils.create(
				ColorUtils.chat("&c라즈베리"),
				ColorUtils.chat("&e&l강화 안내서"),

				ColorUtils.chat("""
                &0&l[ 강화 시스템 안내 ]

                &8■ &1강화 시작하기
                &f일반 장비는 바로 강화할
                &f수 없습니다. 대장간을
                &f방문해 &9[마법강화 적용]&f을
                &f진행해야 합니다.

                &8■ &1강화 재료
                &f강화에는 &#C822FF강화 프리즘&f이
                &f소모됩니다.

                &8■ &1강화 효과
                &f- 무기: 공격력 대폭 증가
                &f- 도구: 채광 속도 증가
                """),

				ColorUtils.chat("""
                &0&l[ 강화 수치 (1~5강) ]

                &81강&f: 프리즘 1개 | &2100%
                &82강&f: 프리즘 1개 | &285%
                &83강&f: 프리즘 1개 | &270%
                &84강&f: 프리즘 3개 | &255%
                &85강&f: 프리즘 3개 | &240%

                &7(배율: x1.10 ~ x1.65)

                &c※ 4강부터 실패 시
                &c   강화 단계가 하락합니다.
                """),

				ColorUtils.chat("""
                &0&l[ 강화 수치 (6~10강) ]

                &86강&f: 프리즘 7개 | &230% &7(&c파괴 1%&7)
                &87강&f: 프리즘 7개 | &220% &7(&c파괴 2%&7)
                &88강&f: 프리즘 15개| &212% &7(&c파괴 3%&7)
                &89강&f: 프리즘 30개| &25%  &7(&c파괴 5%&7)
                &810강&f: &#C822FF최대 강화 (x3.50)&r

                &c※ 6강부터 파괴 위험 발생!
                &c※ 9강 실패 시 2단계 하락!
                &7(방지권 아이템 사용 가능)
                """),

				ColorUtils.chat("""
                &0&l[ 무기 초월 시스템 ]

                &8■ &5초월 조건
                &f- &410강 달성 무기&f만 가능

                &8■ &5초월 파편 획득처
                &f- 티어 승급 (1~2개 획득)
                &f- 상점 구매 (재화/결제)

                &8■ &5필요 속성 재료
                &f메아리조각, 화염구, 돌풍구,
                &f얼음, 엔더진주
                &7(세부 속성과 능력은 현재
                &7베일에 싸여 있습니다...)
                """),

				ColorUtils.chat("""
                &0&l[ 유료 서비스 약관 ]

                &8■ &0디지털 콘텐츠 규정
                &f본 서버의 유료 재화 및
                &f아이템은 전자상거래법상
                &f디지털 콘텐츠에 해당합니다.

                &8■ &0청약철회 제한
                &f구매 즉시 계정에 적용 및
                &f지급이 완료되므로, 지급 후
                &c단순 변심으로 인한 환불은
                &c법적으로 불가합니다.
                """),

				ColorUtils.chat("""
                &0&l[ 환불 규정 및 예외 ]

                &8■ &0환불 가능 조건
                &f- 시스템 오류로 미지급
                &f- 중복 결제 오리발생 시
                &7(오류 발생 7일 이내 문의)

                &8■ &0미성년자 결제
                &f법정대리 동의 없는 결제는
                &f취소 청구가 가능하나, 명의
                &f도용 시 제한될 수 있습니다.
                """),

				ColorUtils.chat("""
                &0&l[ 부정 이용 및 문의 ]

                &8■ &0부정 결제 제재
                &f강제 환불(차지백) 및 결제
                &f도용 적발 시 계정 영구
                &f정지 및 법적 조치됩니다.

                &8■ &0약관 동의 및 문의
                &f결제 진행 시 본 약관에
                &f동의한 것으로 간주합니다.
                &f문의: 공식 디스코드
                """)
		);
	}

	public static ItemStack PrismFractal() {
		var oraxen = OraxenItems.getItemById("prism_fractal");
		var item = oraxen == null ? new ItemStack(Material.PAPER) : oraxen.build();

		return ItemBuilder.of(XmasLegacy.getInstance(), item)
				.name(ColorUtils.chat("&b&l프리즘 조각"))
				.lore(ColorUtils.chat("&7무한한 힘을 가진 아이템이다."),
						ColorUtils.chat("&7장비강화에 사용하여 성장할 수 있으며"),
						ColorUtils.chat("&7강한 힘에는 큰 책임이 따른다."))
				.hideAllFlags()
				.setTag(key, "prism_fractal")
				.setGlint(true)
				.setMaxStackSize(99)
				.addAttribute(Attribute.MOVEMENT_SPEED, -0.1, AttributeModifier.Operation.ADD_NUMBER)
				.addAttribute(Attribute.CAMERA_DISTANCE, -0.1, AttributeModifier.Operation.ADD_NUMBER)
				.build().clone();

	}

	public static ItemStack EssenceOfEnchant() {
		return ItemBuilder.of(XmasLegacy.getInstance(), Material.GHAST_TEAR)
				.name(ColorUtils.chat("강화의 정수"))
				.lore(
						ColorUtils.chat("&7강화 도중 발생한 마력 파편."),
						ColorUtils.chat("&7결합하여 사용자의 도구의 마력을 끌어올려준다."))
				.setGlint(true)
				.setRarity(ItemRarity.UNCOMMON)
				.hideAllFlags()
				.setGlint(true)
				.setTag(key, "essence_enchant")
				.build();
	}

	public static boolean isMaterial(ItemStack item) {
		return KeyUtils.hasKey(item, key, PersistentDataType.STRING, "prism_fractal");
	}
}
