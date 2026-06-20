package xmaslegacy.InfoNpcs;

import org.bukkit.Sound;
import org.lazberry.xmaslegacy.ColorUtils;

import java.util.List;

public class WitchNpc extends AbstractNpc {
	public WitchNpc() {
		super(List.of(
				"자네들의 힘이 자네들을 멸할것이야..",
				"무엇을 하여도 결국에.. 아무것도 아니게돼..",
				"우리를 살려주는 힘이, 우리를 죽이고있는거야..!",
				"아무것도 하지마, 이 세상은 저주받았어.",
				"&c백야&f의 마지막 발버둥일 뿐이라고!",
				"크리아 마을의 결사대는 영웅이 아니야, 그저 &4재앙&f이야.",
				"결사대 모집을 당장 막아야돼!!",
				"...",
				"..",
				"..엘리안 도대체 무슨짓을 한거야.."
		), ColorUtils.chat("&5&l미치광이 마녀"), Sound.ENTITY_WITCH_AMBIENT);
	}
}
