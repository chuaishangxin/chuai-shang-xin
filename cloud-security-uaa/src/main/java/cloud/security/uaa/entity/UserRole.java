package cloud.security.uaa.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author wangpq
 * @version 1.0
 * @className UserRole
 * @description TODO
 * @date 2020/5/27 18:33
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserRole {

    private int id;

    private String name;
}
