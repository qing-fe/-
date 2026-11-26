package com.huang.redblack.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigInteger;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 用户表
 * @TableName users
 */
@TableName(value ="users")
@Data
public class Users {
    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private BigInteger id;

    /**
     * 邮箱（登录用，唯一。删除时置空释放）
     */
    @TableField(value = "email")
    private String email;

    /**
     * 邮箱是否已验证：0-未验证 1-已验证
     */
    @TableField(value = "email_verified")
    private Integer email_verified;

    /**
     * 用户名（展示用，唯一。删除时置空释放）
     */
    @TableField(value = "username")
    private String username;

    /**
     * 密码哈希（bcrypt/argon2）
     */
    @TableField(value = "password_hash")
    private String password_hash;

    /**
     * 头像URL
     */
    @TableField(value = "avatar_url")
    private String avatar_url;

    /**
     * 状态：0-禁用 1-正常
     */
    @TableField(value = "status")
    private Integer status;

    /**
     * 角色：1-普通用户 100-管理员
     */
    @TableField(value = "role")
    private Integer role;

    /**
     * 最后登录时间
     */
    @TableField(value = "last_login_at")
    private LocalDateTime last_login_at;

    /**
     * 创建时间
     */
    @TableField(value = "created_at")
    private LocalDateTime created_at;

    /**
     * 更新时间
     */
    @TableField(value = "updated_at")
    private LocalDateTime updated_at;

    /**
     * 软删除时间（NULL表示未删除）
     */
    @TableField(value = "deleted_at")
    private LocalDateTime deleted_at;

    @Override
    public boolean equals(Object that) {
        if (this == that) {
            return true;
        }
        if (that == null) {
            return false;
        }
        if (getClass() != that.getClass()) {
            return false;
        }
        Users other = (Users) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getEmail() == null ? other.getEmail() == null : this.getEmail().equals(other.getEmail()))
            && (this.getEmail_verified() == null ? other.getEmail_verified() == null : this.getEmail_verified().equals(other.getEmail_verified()))
            && (this.getUsername() == null ? other.getUsername() == null : this.getUsername().equals(other.getUsername()))
            && (this.getPassword_hash() == null ? other.getPassword_hash() == null : this.getPassword_hash().equals(other.getPassword_hash()))
            && (this.getAvatar_url() == null ? other.getAvatar_url() == null : this.getAvatar_url().equals(other.getAvatar_url()))
            && (this.getStatus() == null ? other.getStatus() == null : this.getStatus().equals(other.getStatus()))
            && (this.getRole() == null ? other.getRole() == null : this.getRole().equals(other.getRole()))
            && (this.getLast_login_at() == null ? other.getLast_login_at() == null : this.getLast_login_at().equals(other.getLast_login_at()))
            && (this.getCreated_at() == null ? other.getCreated_at() == null : this.getCreated_at().equals(other.getCreated_at()))
            && (this.getUpdated_at() == null ? other.getUpdated_at() == null : this.getUpdated_at().equals(other.getUpdated_at()))
            && (this.getDeleted_at() == null ? other.getDeleted_at() == null : this.getDeleted_at().equals(other.getDeleted_at()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getEmail() == null) ? 0 : getEmail().hashCode());
        result = prime * result + ((getEmail_verified() == null) ? 0 : getEmail_verified().hashCode());
        result = prime * result + ((getUsername() == null) ? 0 : getUsername().hashCode());
        result = prime * result + ((getPassword_hash() == null) ? 0 : getPassword_hash().hashCode());
        result = prime * result + ((getAvatar_url() == null) ? 0 : getAvatar_url().hashCode());
        result = prime * result + ((getStatus() == null) ? 0 : getStatus().hashCode());
        result = prime * result + ((getRole() == null) ? 0 : getRole().hashCode());
        result = prime * result + ((getLast_login_at() == null) ? 0 : getLast_login_at().hashCode());
        result = prime * result + ((getCreated_at() == null) ? 0 : getCreated_at().hashCode());
        result = prime * result + ((getUpdated_at() == null) ? 0 : getUpdated_at().hashCode());
        result = prime * result + ((getDeleted_at() == null) ? 0 : getDeleted_at().hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", id=").append(id);
        sb.append(", email=").append(email);
        sb.append(", email_verified=").append(email_verified);
        sb.append(", username=").append(username);
        sb.append(", password_hash=").append(password_hash);
        sb.append(", avatar_url=").append(avatar_url);
        sb.append(", status=").append(status);
        sb.append(", role=").append(role);
        sb.append(", last_login_at=").append(last_login_at);
        sb.append(", created_at=").append(created_at);
        sb.append(", updated_at=").append(updated_at);
        sb.append(", deleted_at=").append(deleted_at);
        sb.append("]");
        return sb.toString();
    }
}