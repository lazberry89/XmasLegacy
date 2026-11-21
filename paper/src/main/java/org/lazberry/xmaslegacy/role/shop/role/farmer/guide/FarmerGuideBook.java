package org.lazberry.xmaslegacy.role.shop.role.farmer.guide;

import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.lazberry.xmaslegacy.role.shop.role.farmer.guide.BookLayout.Section;
import org.lazberry.xmaslegacy.utils.ColorUtils;

import java.util.List;

/**
 * Farming guide for the CustomCrops content, written from the server's own CustomCrops config
 * (plugins/CustomCrops/contents). Update the text here when those configs change.
 */
public final class FarmerGuideBook {
	private FarmerGuideBook() {}

	static final List<Section> SECTIONS = List.of(
			new Section(null, List.of(
					"&2&l  농부 가이드북",
					"",
					"&0새로 추가된 작물, 씨앗, 농사 도구 사용법을 정리했습니다.",
					"",
					"&81. 농사 시작하기",
					"&82. 물 관리",
					"&83. 계절",
					"&84. 물뿌리개와 스프링클러",
					"&85. 비료",
					"&86. 그 밖의 도구",
					"&87. 품질과 판매",
					"&88. 작물 도감"
			)),
			new Section("&21. 농사 시작하기", List.of(
					"&0- 도구상점에서 화분을 사서 땅에 설치합니다.",
					"&0- 씨앗을 들고 화분을 우클릭하면 심어집니다.",
					"&0- 화분에 물을 주어야 작물이 자랍니다.",
					"&0- 다 자란 작물은 맨손으로 우클릭하거나 부수면 수확됩니다.",
					"",
					"&4작물은 화분 위에만 심을 수 있습니다."
			)),
			new Section("&22. 물 관리", List.of(
					"&0- 화분은 물을 최대 5칸까지 담습니다.",
					"&0- 비가 오면 빗물을 흡수합니다.",
					"&0- 물 양동이는 3칸, 물병은 1칸을 채웁니다.",
					"&0- 물이 0이면 자라지 않습니다.",
					"",
					"&4물이 마르면 토마토, 포도가 시들 수 있습니다."
			)),
			new Section("&23. 계절", List.of(
					"&0- 봄, 여름, 가을, 겨울이 마크 시간으로 28일씩 돌아갑니다.",
					"&0- 작물마다 심을 수 있는 계절과 자라는 계절이 정해져 있습니다.",
					"&0- 맞지 않는 계절이 오면 작물이 시듭니다.",
					"",
					"&1팁: 온실 유리가 반경 5칸 작물이 계절로 시드는 것을 막아줍니다."
			)),
			new Section("&24. 물뿌리개", List.of(
					"&0물 블록을 우클릭해 채우고, 화분이나 스프링클러에 우클릭해 물을 줍니다.",
					"",
					"&8구리: &03번 사용, 1칸",
					"&8강철: &04번 사용, 앞 1x3칸",
					"&8황금: &05번 사용, 2x2칸",
					"&8이리듐: &03번 사용, 3x2칸",
					"",
					"&0한 번 줄 때 화분에 물 1칸이 찹니다."
			)),
			new Section("&24. 스프링클러", List.of(
					"&0화분 옆에 설치하면 담긴 물로 주변 화분에 자동으로 물을 줍니다.",
					"&0물 양동이(3칸), 물병(1칸), 물뿌리개로 채웁니다. 최대 4칸 저장.",
					"",
					"&8기본: &0상하좌우 4칸",
					"&8고급: &0둘러싼 8칸",
					"&8최신형: &0주변 5x5칸"
			)),
			new Section("&25. 비료 기본", List.of(
					"&0- 비료를 들고 화분을 우클릭해 뿌립니다.",
					"&0- 화분 하나에 비료는 1개만 들어갑니다.",
					"&0- 효과 횟수: 비료와 보습토 28회, 나머지 14회",
					"",
					"&4비료(품질), 수확 증가제, 성장 촉진제는 씨앗을 심기 전에 뿌려야 합니다."
			)),
			new Section("&25. 비료 (품질)", List.of(
					"&0금별, 은별이 나올 확률을 올립니다.",
					"",
					"&8비료 없음: &085 / 10 / 5%",
					"&8기본: &070 / 20 / 10%",
					"&8고급: &055 / 30 / 15%",
					"&8최고급: &040 / 40 / 20%",
					"",
					"&8(일반 / 은별 / 금별)"
			)),
			new Section("&25. 수확 증가제", List.of(
					"&0수확할 때 작물이 더 나옵니다.",
					"",
					"&8기본: &0대부분 +1, 가끔 +2~3",
					"&8고급: &0항상 +1, 자주 +2",
					"&8최고급: &0항상 +2, 40%로 +3"
			)),
			new Section("&25. 성장 촉진제, 보습토", List.of(
					"&5성장 촉진제",
					"&0자랄 때 단계가 더 오릅니다.",
					"&8기본: &050%로 1단계 더",
					"&8고급: &070%로 1단계, 10%로 2단계",
					"&8최고급: &0최대 3단계",
					"",
					"&5보습토",
					"&0물이 줄지 않을 확률",
					"&8기본 10%, 고급 20%, 최고급 30%"
			)),
			new Section("&25. 변이 물약", List.of(
					"&0작물이 변이할 확률을 올립니다.",
					"&8초급 +2%, 중급 +4%, 고급 +8%",
					"",
					"&0변이하면 거대 작물이나 황금 토마토가 생깁니다.",
					"",
					"&1팁: 변이 물약과 보습토는 심은 뒤에도 뿌릴 수 있습니다."
			)),
			new Section("&26. 그 밖의 도구", List.of(
					"&5허수아비",
					"&0반경 7칸 안의 작물을 까마귀로부터 지켜줍니다.",
					"",
					"&5온실 유리",
					"&0반경 5칸 안의 작물이 계절 때문에 시들지 않게 해줍니다.",
					"",
					"&5토양 측정기",
					"&0화분을 우클릭하면 물과 비료 상태를 보여줍니다."
			)),
			new Section("&26. 까마귀와 뼛가루", List.of(
					"&5까마귀",
					"&0가끔 까마귀가 날아와 작물을 쪼아 먹고, 잠시 뒤 작물이 사라집니다. 허수아비로 막으세요.",
					"",
					"&5뼛가루",
					"&0작물에 쓰면 60% 확률로 1단계, 20% 확률로 2단계 자랍니다."
			)),
			new Section("&27. 품질과 판매", List.of(
					"&0수확한 작물은 일반, 은별, 금별 품질로 나옵니다.",
					"",
					"&0농부상점 작물판매에서 팔 수 있고, 은별은 일반의 2배, 금별은 4배 가격입니다.",
					"",
					"&0쉬프트+클릭하면 가진 작물을 한 번에 모두 팝니다."
			)),
			crop("토마토", "봄, 가을", "1~4개, 씨앗 1~2개",
					"&0다 자란 뒤 맨손 우클릭으로 수확하면 자동으로 다시 심어집니다.",
					"&4물이 마르면 시들 수 있고, 겨울에 시듭니다.",
					"&1팁: 드물게 황금 토마토로 변이합니다. 황금 괭이로 수확하면 황금 토마토 1~4개!"),
			crop("양배추", "봄, 여름", "1개, 씨앗 1~2개",
					"&4겨울에 시듭니다.",
					"&1팁: 드물게 거대 양배추로 자랍니다."),
			crop("배추", "봄, 여름, 가을", "1개, 씨앗 1~2개",
					"&4겨울에 시듭니다.",
					"&1팁: 세 계절 동안 키울 수 있어 무난한 작물입니다."),
			crop("옥수수", "여름, 가을", "1개, 씨앗 1~2개",
					"&4봄과 겨울에 시듭니다."),
			crop("가지", "여름, 가을에 심기", "2~5개, 씨앗 1~2개",
					"&0여름에만 자랍니다.",
					"&4겨울에 시듭니다.",
					"&1팁: 여름 초에 심어야 다 자랍니다."),
			crop("마늘", "계절 상관없음", "4개 고정, 씨앗 1~2개",
					"&0계절에 상관없이 심을 수 있고 시들지도 않습니다.",
					"&1팁: 뼛가루로 키우면 빠릅니다."),
			crop("포도", "봄, 여름, 가을", "3~5개, 씨앗 50%",
					"&0자라는 데 오래 걸리지만, 수확해도 덩굴이 남아 다시 열매를 맺습니다.",
					"&4겨울에 시들고, 여름에 물이 마르면 시듭니다."),
			crop("홉", "여름", "2~5개",
					"&0수확하면 자동으로 다시 심어집니다. 씨앗은 나오지 않습니다.",
					"&4여름이 아니면 시듭니다."),
			crop("고추", "봄, 가을", "3~6개, 씨앗 50%",
					"&4겨울에 시듭니다.",
					"&1팁: 한 번에 많이 나와 돈벌이에 좋습니다."),
			crop("파인애플", "여름", "1개, 씨앗 1개",
					"&4여름이 아니면 시듭니다.",
					"&1팁: 드물게 거대 파인애플로 자랍니다."),
			crop("용과", "봄, 여름, 가을에 자람", "3~5개, 씨앗 1~2개",
					"&0나무처럼 자라며 물이 필요 없고 시들지 않습니다.",
					"&0수확해도 나무가 남아 다시 열매를 맺습니다. 원목도 나옵니다."),
			crop("홍바오", "겨울", "1~4개",
					"&0겨울에만 키울 수 있는 특별한 작물입니다.",
					"&4겨울이 아니면 시듭니다.",
					"&1팁: 크리스마스 시즌을 놓치지 마세요!")
	);

	private static Section crop(String name, String season, String harvest, String... notes) {
		List<String> lines = new java.util.ArrayList<>(List.of(
				"&8계절: &0" + season,
				"&8수확: &0" + harvest,
				""));
		lines.addAll(List.of(notes));
		return new Section("&28. 작물 도감 - " + name, lines);
	}

	public static ItemStack create() {
		ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
		BookMeta meta = (BookMeta) book.getItemMeta();
		meta.setTitle("농부 가이드북");
		meta.setAuthor("XmasLegacy");
		meta.setGeneration(BookMeta.Generation.ORIGINAL);
		List<Component> pages = BookLayout.paginate(SECTIONS).stream().map(ColorUtils::chat).toList();
		meta.pages(pages);
		meta.displayName(ColorUtils.chat("&6&l농부 가이드북").decoration(net.kyori.adventure.text.format.TextDecoration.ITALIC, false));
		meta.lore(List.of(ColorUtils.chat("&7작물, 씨앗, 농사 도구 사용법이 담긴 책입니다.")));
		book.setItemMeta(meta);
		return book;
	}
}
