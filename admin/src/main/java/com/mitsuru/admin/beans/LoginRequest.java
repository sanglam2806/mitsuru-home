package  com.mitsuru.admin.beans;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
public class LoginRequest {

	@Getter @Setter
	private String username;
	@Getter @Setter
	private String password;
}
