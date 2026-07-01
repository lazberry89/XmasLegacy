package org.lazberry.xmaslegacy.InfoNpcs;

import org.bukkit.Sound;
import org.lazberry.xmaslegacy.ColorUtils;

import java.util.List;

public class LibrarianNpc extends AbstractNpc {
	public LibrarianNpc() {
		super(List.of(
				"도서관에 용케 잘 찾아왔군? 다 똑같은 건물들 뿐일텐데.",
				"잘 찾아왔어. 살아남은 &6조상&f들의 이야기가 모두 담겨있지.",
				"책에는 자네가 살아갈 수 있도록 도와주는 엄청난 양의 정보가 적혀있어.",
				"도서관은 이 마을에서 가장 귀중한 장소라고 할 수 있지.",
				"근데, 누군가 책 몇개를 반납을 안해. 분명 민간 주택 어딘가에 있을거야.",
				"꼭 나이드신 분들이 빌려가서 반납을 안한다니깐."
		), ColorUtils.chat("&a&l사서"), Sound.ENTITY_VILLAGER_AMBIENT, NpcType.LIBRARIAN);
	}
}
