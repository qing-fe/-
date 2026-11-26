package com.huang.redblack.pojo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigInteger;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 认证日志（登录/注册/改密）
 * @TableName user_auth_logs
 */
@TableName(value ="user_auth_logs")
@Data
public class UserAuthLogs {
    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private BigInteger id;

    /**
     * 用户ID（注册失败、登录失败时可能为NULL）
     */
    @TableField(value = "user_id")
    private BigInteger user_id;

    /**
     * 操作时的邮箱
     */
    @TableField(value = "email")
    private String email;

    /**
     * 操作类型：REGISTER/LOGIN/LOGOUT/RESET_PASSWORD
     */
    @TableField(value = "operation")
    private String operation;

    /**
     * 结果：1-成功 0-失败
     */
    @TableField(value = "result")
    private Integer result;

    /**
     * 失败原因
     */
    @TableField(value = "fail_reason")
    private String fail_reason;

    /**
     * 客户端IP
     */
    @TableField(value = "ip")
    private String ip;

    /**
     * 浏览器UA
     */
    @TableField(value = "user_agent")
    private String user_agent;

    /**
     * 创建时间
     */
    @TableField(value = "created_at")
    private LocalDateTime created_at;

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
        UserAuthLogs other = (UserAuthLogs) that;
        return (this.getId() == null ? other.getId() == null : this.getId().equals(other.getId()))
            && (this.getUser_id() == null ? other.getUser_id() == null : this.getUser_id().equals(other.getUser_id()))
            && (this.getEmail() == null ? other.getEmail() == null : this.getEmail().equals(other.getEmail()))
            && (this.getOperation() == null ? other.getOperation() == null : this.getOperation().equals(other.getOperation()))
            && (this.getResult() == null ? other.getResult() == null : this.getResult().equals(other.getResult()))
            && (this.getFail_reason() == null ? other.getFail_reason() == null : this.getFail_reason().equals(other.getFail_reason()))
            && (this.getIp() == null ? other.getIp() == null : this.getIp().equals(other.getIp()))
            && (this.getUser_agent() == null ? other.getUser_agent() == null : this.getUser_agent().equals(other.getUser_agent()))
            && (this.getCreated_at() == null ? other.getCreated_at() == null : this.getCreated_at().equals(other.getCreated_at()));
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
        result = prime * result + ((getUser_id() == null) ? 0 : getUser_id().hashCode());
        result = prime * result + ((getEmail() == null) ? 0 : getEmail().hashCode());
        result = prime * result + ((getOperation() == null) ? 0 : getOperation().hashCode());
        result = prime * result + ((getResult() == null) ? 0 : getResult().hashCode());
        result = prime * result + ((getFail_reason() == null) ? 0 : getFail_reason().hashCode());
        result = prime * result + ((getIp() == null) ? 0 : getIp().hashCode());
        result = prime * result + ((getUser_agent() == null) ? 0 : getUser_agent().hashCode());
        result = prime * result + ((getCreated_at() == null) ? 0 : getCreated_at().hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", id=").append(id);
        sb.append(", user_id=").append(user_id);
        sb.append(", email=").append(email);
        sb.append(", operation=").append(operation);
        sb.append(", result=").append(result);
        sb.append(", fail_reason=").append(fail_reason);
        sb.append(", ip=").append(ip);
        sb.append(", user_agent=").append(user_agent);
        sb.append(", created_at=").append(created_at);
        sb.append("]");
        return sb.toString();
    }
}