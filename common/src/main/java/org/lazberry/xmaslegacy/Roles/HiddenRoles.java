package org.lazberry.xmaslegacy.Roles;

import org.jetbrains.annotations.Nullable;
import org.lazberry.xmaslegacy.settings.Lang;

import java.util.List;

@SuppressWarnings("unused")
public enum HiddenRoles implements Role {
	DRAGON_SLAYER(null, "&#DD00FF[&#E228FF드&#E850FF래&#ED78FF곤 &#F8C8FF슬&#F8C8FF레&#F8C8FF이&#F8C8FF어&#F8C8FF]", "&#DD00FF[&#E11DFFD&#E539FFr&#E956FFa&#EC72FFg&#F08FFFo&#F4ABFFn &#F8C8FFS&#F8C8FFl&#F8C8FFa&#F8C8FFy&#F8C8FFe&#F8C8FFr&#F8C8FF]"),
	JOKER(null, "&#00FFA2[&#4CFFBE조&#97FFD9커&#E2E2E2]", "[Joker]"),
	SANTA_CLAUS(Roles.MERCHANT, "&#FF0000[&#F82E2E산&#F15C5C타&#EA8989클&#E3B7B7로&#EFD4DD스&#EFD4DD]", "&#FF0000[&#FC1717S&#F82E2Ea&#F54545n&#F15C5Ct&#EE7272a&#EA8989C&#E7A0A0l&#E3B7B7a&#EFD4DDu&#EFD4DDs&#EFD4DD]"),
	ORDER_OF_KNIGHTS(Roles.PRIEST, "&#E0C171[&#E4CA85신&#E9D39A의 &#F2E4C2기&#F6EDD6사&#FBF6EB단&#FFFFFF]", "&#E0C171[&#E2C579O&#E4C882r&#E5CC8Ad&#E7D092e&#E9D39Br &#EDDBABO&#EFDEB4f &#F2E5C5K&#F4E9CDn&#F6EDD5i&#F8F0DEg&#FAF4E6h&#FBF8EEt&#FDFBF7s&#FFFFFF]"),
	YONKO(null, "&#8C0000[&#C60303사&#FF0606황&#980000]", "&#8C0000[&#B20202よ&#D90404ん&#FF0606こ&#CC0303う&#980000]");

	HiddenRoles(Role parent ,String ko, String en) {
		this.Kor = ko;
		this.Eng = en;
		this.parent = parent;
	}

	private final String Kor;
	private final String Eng;
	private final Role parent;

    /**
     *
     * @param la Language
     * @return kor or Eng
     */
	public String getName(Lang la) {
		return la.equals(Lang.KOREAN) ? this.Kor : this.Eng;
	}

	@Override
	public String getKor() {
		return this.Kor;
	}

	@Override
	public @Nullable Role parent() {
		return this.parent;
	}

	@Override
	public int getTier() {
		return 4;
	}

	@Override
	public List<Role> next() {
		return List.of();
	}
}
