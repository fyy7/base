package com.kind.base.core.auth;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.kind.common.constant.ConCommon;
import com.kind.common.utils.ConvertSql;
import com.kind.common.utils.ConvertSqlDefault;
import com.kind.framework.auth.ResourceInfo;
import com.kind.framework.auth.SubjectAuthInfo.ResourceGroupType;
import com.kind.framework.cache.CacheManager;
import com.kind.framework.cache.redis.AbstractRedisClientManager;
import com.kind.framework.cache.redis.IRedisClient;
import com.kind.framework.cache.redis.KryoRedisSerializer;
import com.kind.framework.core.DbHelper;
import com.kind.framework.core.bean.ResultBean;
import com.kind.framework.core.bean.UserPO;
import com.kind.framework.service.IdGenerator;
import com.kind.framework.tree.Tree;
import com.kind.framework.tree.TreeNode;
import com.kind.framework.tree.TreeService;
import com.kind.framework.utils.SpringUtil;
import com.kind.framework.utils.StringUtil;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;

/**
 * 资源服务类<br>
 * [sys_resources表需新增字段：channel_rtype]
 * 
 * @author yanhang
 */
@Service
public class ResourceService extends DbHelper {

	// 机构管理员设置虚拟部门ID
	private static final String ORG_ADMIN_DEPT = "DEPT_ID";

	private com.kind.framework.log.Logger log = com.kind.framework.log.LogService.getLogger(ResourceService.class);

	private KryoRedisSerializer<Map<String, Set<ResourceInfo>>> serializer = new KryoRedisSerializer<Map<String, Set<ResourceInfo>>>();

	/**
	 * 全局权限回收
	 * 
	 * @param conn
	 * @return
	 */
	public int recoveryRights() {

		// 来源于角色权限的非法权限验证语句与删除语句(需要改善增加上级部门授权合法的判断，当前是B.DEPT_ID=C.DEPT_ID)
		StringBuffer selRoleRightsSql = new StringBuffer("SELECT COUNT(1) AS NUM FROM SYS_N_ROLERIGHTS A WHERE NOT EXISTS(SELECT 1 FROM SYS_N_ROLES B,V_USER_RESOURCES C WHERE A.ROLEID=B.ROLEID AND B.CREATOR=C.OPNO AND A.RESOURCEID=C.RID AND (B.DEPT_ID=C.DEPT_ID OR C.DEPT_ID='DEPT_ID') )");
		StringBuffer delRoleRightsSql = new StringBuffer("DELETE FROM SYS_N_ROLERIGHTS WHERE NOT EXISTS(SELECT 1 FROM SYS_N_ROLES B,V_USER_RESOURCES C WHERE B.CREATOR=C.OPNO AND SYS_N_ROLERIGHTS.ROLEID=B.ROLEID AND SYS_N_ROLERIGHTS.RESOURCEID=C.RID AND (B.DEPT_ID=C.DEPT_ID OR C.DEPT_ID='DEPT_ID') )");

		// 来源于用户权限管理的非法权限验证语句与删除语句
		StringBuffer selUserRightSql = new StringBuffer("SELECT COUNT(1) AS NUM FROM SYS_N_USERRIGHTS A WHERE NOT EXISTS(SELECT 1 FROM V_USER_RESOURCES B WHERE A.CREATOR=B.OPNO AND A.RESOURCEID=B.RID AND (A.CREATOR_DEPT_ID=B.DEPT_ID OR B.DEPT_ID='DEPT_ID') )");
		StringBuffer delUserRightSql = new StringBuffer("DELETE FROM SYS_N_USERRIGHTS WHERE NOT EXISTS(SELECT 1 FROM V_USER_RESOURCES B WHERE SYS_N_USERRIGHTS.CREATOR=B.OPNO AND SYS_N_USERRIGHTS.RESOURCEID=B.RID AND (SYS_N_USERRIGHTS.CREATOR_DEPT_ID=B.DEPT_ID OR B.DEPT_ID='DEPT_ID') )");

		// 来源于部门职位权限管理的非法权限验证语句与删除语句
		StringBuffer selDeptOfficeRightSql = new StringBuffer("SELECT COUNT(1) AS NUM FROM SYS_N_ROLES_DEPT_OFFICE_RIGHTS A WHERE   NOT EXISTS(SELECT 1 FROM V_USER_RESOURCES C,SYS_N_ROLES_DEPT_OFFICE B WHERE B.CREATE_OPNO=C.OPNO AND A.ROLEID=B.ROLEID AND A.RESOURCEID=RID)");
		StringBuffer delDeptOfficeRightSql = new StringBuffer("DELETE FROM SYS_N_ROLES_DEPT_OFFICE_RIGHTS WHERE NOT EXISTS(SELECT 1 FROM V_USER_RESOURCES C,SYS_N_ROLES_DEPT_OFFICE B WHERE B.CREATE_OPNO=C.OPNO AND SYS_N_ROLES_DEPT_OFFICE_RIGHTS.ROLEID=B.ROLEID AND SYS_N_ROLES_DEPT_OFFICE_RIGHTS.RESOURCEID=RID)");

		Boolean check = true;// 是否需要验证
		try {
			while (check) {
				check = false;

				String roleRightsNum = String.valueOf(this.getOneFiledValue(null, selRoleRightsSql.toString(), Arrays.asList()));

				if (!"0".equals(roleRightsNum)) {
					this.executeSql(null, delRoleRightsSql.toString(), Arrays.asList());

					check = true;
				}

				String userRightNum = String.valueOf(this.getOneFiledValue(null, selUserRightSql.toString(), Arrays.asList()));
				if (!"0".equals(userRightNum)) {
					this.executeSql(null, delUserRightSql.toString(), Arrays.asList());
					check = true;
				}

				String deptOfficeRight = String.valueOf(this.getOneFiledValue(null, selDeptOfficeRightSql.toString(), Arrays.asList()));
				if (!"0".equals(deptOfficeRight)) {
					this.executeSql(null, delDeptOfficeRightSql.toString(), Arrays.asList());
					check = true;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		}

		return 1;
	}

	/**
	 * UserPO获取用户权限
	 * 
	 */
	public Set<ResourceInfo> getUserResourcesByUserPO(UserPO userPo, String channelId, String channelRType) {
		// 判断是否委托
		if (userPo.getConsignorType() == 1) {
			return getUserResourcesEntrust(userPo, channelId, channelRType);
		} else {
			return getUserResources(userPo, channelId, channelRType);
		}

	}

	/******************************* Entrust Start ****************************************************/

	public void cleanRedisCache(UserPO userPo) {
		String redisPermissionKey = SpringUtil.getEnvProperty(ConCommon.CFGKEY__REDIS_KEY_PERMISSION, "");
		String userAccount;
		if (userPo.getConsignorType() != 1) {
			userAccount = userPo.getOpAccount();
		} else {
			userAccount = userPo.getConsignorSysUserPO().getOpAccount();
		}
		AbstractRedisClientManager redisClientManager = CacheManager.getRedisClientManager();
		IRedisClient client = redisClientManager.getClient(SpringUtil.getEnvProperty("app.system.resource.redisName", "default"));
		client.hDel(redisPermissionKey, userAccount);

	}

	/**
	 * 根据用户po获取委托授权资源
	 * 
	 * @param userPo
	 * @param mainSet
	 * @return
	 */
	public Set<ResourceInfo> getUserResourcesEntrust(UserPO userPo, String channelId, String channelRType) {
		if (userPo.getConsignorType() != 1) {
			return null;
		}
		UserPO ConsignorUserPO = userPo.getConsignorSysUserPO();
		// 委托人的账号
		String userAccount = ConsignorUserPO.getOpAccount();
		// Redis里整体存储的KEY
		String redisPermissionKey = SpringUtil.getEnvProperty(ConCommon.CFGKEY__REDIS_KEY_PERMISSION, "");
		// 存储具体用户资源信息的KEY//委托人不另外设置id
		String redisUserPermissionKey = String.format("%s_%s_%s", channelId, channelRType, ConsignorUserPO.getDeptId());

		AbstractRedisClientManager redisClientManager = CacheManager.getRedisClientManager();
		IRedisClient client = redisClientManager.getClient(SpringUtil.getEnvProperty("app.system.resource.redisName", "default"));

		Object cache = StrUtil.isNotBlank(redisPermissionKey) ? client.hGet(redisPermissionKey, userAccount) : null;

		if (cache != null) {
			Map<String, Set<ResourceInfo>> deptResources = serializer.deserialize((byte[]) cache);
			if (deptResources.get(redisUserPermissionKey) != null) {
				return deptResources.get(redisUserPermissionKey);
			}
		}
		// 委托资源获取
		Set<ResourceInfo> userRoleInDeptResources = getUserRoleInDeptResourcesEntrust(userPo.getOpNo(), userPo.getDeptId(), ConsignorUserPO.getOpNo(), ConsignorUserPO.getDeptId(), channelId, channelRType);

		Set<ResourceInfo> all = new HashSet<ResourceInfo>();
		all.addAll(userRoleInDeptResources);
		// 保存到redis
		if (StrUtil.isNotBlank(redisPermissionKey)) {
			Map<String, Set<ResourceInfo>> deptResources = null;
			if (client.hGet(redisPermissionKey, userAccount) == null) {
				deptResources = new HashMap<String, Set<ResourceInfo>>();
			} else {
				deptResources = serializer.deserialize((byte[]) client.hGet(redisPermissionKey, userAccount));
			}
			deptResources.put(redisUserPermissionKey, all);
			client.hSet(redisPermissionKey, userAccount, serializer.serialize(deptResources));
		}

		return all;
	}

	/**
	 * 获取[被委托人]授权的资源
	 * 
	 * @param userId
	 * @param deptId
	 * @return
	 */
	private Set<ResourceInfo> getUserRoleInDeptResourcesEntrust(String fromUserId, String fromDeptId, String toUserId, String toDeptId, String channelId, String channelRType) {
		StringBuffer sql = new StringBuffer(" SELECT RU.ROLEID RGROUP_ID, RU.OPNO, RU.DEPT_ID, R.RID, R.PARENTID, R.TITLE, R.ALLORDIDX, R.APP_ID, R.RLOGO, R.RTYPE, R.CHANNEL_RTYPE, RB.BVALUE ");
		sql.append(" FROM (SELECT T.OPNO, T.DEPT_ID, T.ROLEID FROM SYS_N_ROLEUSER_ENTRUST T WHERE T.OPNO = ? AND T.DEPT_ID = ?) RU ");
		sql.append(" LEFT JOIN SYS_N_ROLERIGHTS_ENTRUST RR ON RU.ROLEID = RR.ROLEID ");
		sql.append(" LEFT JOIN SYS_RESOURCES R ON RR.RESOURCEID = R.RID ");
		sql.append(" LEFT JOIN SYS_RESOURCES_BUTTON RB ON R.RID = RB.RID ");
		sql.append(" WHERE EXISTS(SELECT 1 FROM SYS_N_ROLES_ENTRUST WHERE RU.ROLEID=ROLEID AND CREATOR=? AND DEPT_ID=?  ) ");

		List<Object> paramList = new ArrayList<Object>();
		paramList.add(toUserId);
		paramList.add(toDeptId);
		paramList.add(fromUserId);
		paramList.add(fromDeptId);
		if (StrUtil.isNotBlank(channelId)) {
			sql.append(" AND R.APP_ID = ? ");
			paramList.add(channelId);
		}

		if (StrUtil.isNotBlank(channelRType)) {
			if (channelRType.indexOf(",") != -1) {
				String rtypes = StringUtil.getNewSplitedStr(channelRType, ",", ",", "'");
				sql.append(" AND R.CHANNEL_RTYPE IN ( " + rtypes + " ) ");
			} else {
				sql.append(" AND R.CHANNEL_RTYPE = ? ");
				paramList.add(channelRType);
			}
		}

		List<Map<String, Object>> queryResult = this.getSelectList(null, sql.toString(), paramList);

		return getResources(ResourceGroupType.ROLE_IN_DEPT, fromDeptId, queryResult);
	}

	/******************************* Entrust end ******************************************************/

	/**
	 * 根据用户帐号、当前登录部门获取用户资源
	 * 
	 * @param userId
	 * @param channelId
	 * @param channelRType
	 * @param departmentId
	 * @param organId
	 * @return
	 */
	public Set<ResourceInfo> getUserResources(String userId, String channelId, String channelRType, String departmentId, String organId) {
		List<Object> paramList = new ArrayList<Object>();
		StringBuffer sql = new StringBuffer(" SELECT U.OPACCOUNT, U.OPTYPE FROM SYS_N_USERS U WHERE U.ISDEL = 0 AND U.ENABLED = 1 AND U.OPNO = ? ");
		paramList.add(userId);

		Map<String, Object> userAccount = this.getSelectMap(null, sql.toString(), paramList);
		if (userAccount == null) {
			return new HashSet<ResourceInfo>();
		}

		int optype = userAccount.get("OPTYPE") != null ? Integer.parseInt(String.valueOf(userAccount.get("OPTYPE"))) : 2;
		if (optype == 0) {
			return new HashSet<ResourceInfo>();
		} else if (optype == 1) {
			UserPO userPO = new UserPO();
			userPO.setOpNo(userId);
			userPO.setDeptId("DEPT_ID");
			userPO.setOpType(optype);
			userPO.setOpAccount(userAccount.get("OPACCOUNT") != null ? (String) userAccount.get("OPACCOUNT") : "");
			userPO.setOrgId(organId);

			return getUserResources(userPO, channelId, channelRType);
		} else {
			paramList = new ArrayList<Object>();
			sql = new StringBuffer(" SELECT U.OPACCOUNT, U.OPTYPE FROM SYS_N_USERS U, SYS_N_USER_DEPT_INFO D WHERE D.OPNO = U.OPNO AND U.OPNO = ? AND D.DEPT_ID = ? AND U.ISDEL = 0 AND U.ENABLED = 1 ");
			paramList.add(userId);
			paramList.add(departmentId);
			if (StrUtil.isNotBlank(organId)) {
				sql.append("AND D.ORGAN_ID = ? ");
				paramList.add(organId);
			}

			userAccount = this.getSelectMap(null, sql.toString(), paramList);
			if (userAccount == null) {
				return new HashSet<ResourceInfo>();
			}

			UserPO userPO = new UserPO();
			userPO.setOpNo(userId);
			userPO.setDeptId(departmentId);
			userPO.setOpType(userAccount.get("OPTYPE") != null ? Integer.parseInt(String.valueOf(userAccount.get("OPTYPE"))) : 2);
			userPO.setOpAccount(userAccount.get("OPACCOUNT") != null ? (String) userAccount.get("OPACCOUNT") : "");
			userPO.setOrgId(organId);

			return getUserResources(userPO, channelId, channelRType);
		}

	}

	/**
	 * 根据用户帐号、当前登录部门获取用户资源 @Title: getUserResources @date 2018年4月24日 @author yanhang @Description: 根据用户帐号、当前登录部门获取用户资源 @param userPO @param channelId @param channelRType @return @throws
	 */
	public Set<ResourceInfo> getUserResources(UserPO userPO, String channelId, String channelRType) {
		String departmentId = userPO.getOpType() == 1 ? ORG_ADMIN_DEPT : userPO.getDeptId();
		String userAccount = userPO.getOpAccount();

		// Redis里整体存储的KEY
		String redisPermissionKey = SpringUtil.getEnvProperty(ConCommon.CFGKEY__REDIS_KEY_PERMISSION, "");
		// 存储具体用户资源信息的KEY
		String redisUserPermissionKey = String.format("%s_%s_%s", channelId, channelRType, departmentId);

		AbstractRedisClientManager redisClientManager = CacheManager.getRedisClientManager();
		IRedisClient client = redisClientManager.getClient(SpringUtil.getEnvProperty("app.system.resource.redisName", "default"));
		// cache: <ConCommon.REDIS_KEY_PERMISSION, <subjectId, <redisUserPermissionKey, <ResourceInfoSet>>>>
		Object cache = StrUtil.isNotBlank(redisPermissionKey) ? client.hGet(redisPermissionKey, userAccount) : null;
		if (cache != null) {
			Map<String, Set<ResourceInfo>> deptResources = serializer.deserialize((byte[]) cache);
			if (deptResources.get(redisUserPermissionKey) != null) {
				return deptResources.get(redisUserPermissionKey);
			}
		}

		// 获取用户直接授权的资源[范围细分到部门，基于部门]
		Set<ResourceInfo> userDirectResources = getUserDirectResources(userPO, channelId, channelRType);

		// 获取[部门里的角色，属于部门的角色]授权的资源
		Set<ResourceInfo> userRoleInDeptResources = getUserRoleInDeptResources(userPO, channelId, channelRType);

		// 获取[部门的角色，分配给部门的角色]授权的资源
		Set<ResourceInfo> userDeptRoleResources = getUserDeptRoleResources(userPO, channelId, channelRType);

		// 获取[部门类型+职务类型，同时满足]授权的资源
		Set<ResourceInfo> userDeptAndOfficeTypeResources = getUserDeptAndOfficeTypeResources(userPO, channelId, channelRType);

		Set<ResourceInfo> all = new HashSet<ResourceInfo>();
		all.addAll(userDirectResources);
		all.addAll(userRoleInDeptResources);
		all.addAll(userDeptRoleResources);
		all.addAll(userDeptAndOfficeTypeResources);

		// 保存到redis
		if (StrUtil.isNotBlank(redisPermissionKey)) {
			Map<String, Set<ResourceInfo>> deptResources = null;
			if (client.hGet(redisPermissionKey, userAccount) == null) {
				deptResources = new HashMap<String, Set<ResourceInfo>>();
			} else {
				deptResources = serializer.deserialize((byte[]) client.hGet(redisPermissionKey, userAccount));
			}
			deptResources.put(redisUserPermissionKey, all);
			client.hSet(redisPermissionKey, userAccount, serializer.serialize(deptResources));
		}

		return all;
	}

	/**
	 * 获取用户直接授权的资源[范围细分到部门，基于部门] @Title: getUserDirectResources @date 2018年4月24日 @author yanhang @Description: 获取用户直接授权的资源[范围细分到部门，基于部门] @param userPO @param channelId @param channelRType @return @throws
	 */
	private Set<ResourceInfo> getUserDirectResources(UserPO userPO, String channelId, String channelRType) {
		StringBuffer sql = new StringBuffer(" SELECT UR.OPNO RGROUP_ID, UR.DEPT_ID, R.RID, R.PARENTID, R.TITLE, R.ALLORDIDX, R.APP_ID, R.RLOGO, R.RTYPE, R.CHANNEL_RTYPE, RB.BVALUE ");
		sql.append(" FROM (SELECT T.OPNO, T.DEPT_ID, T.RESOURCEID FROM SYS_N_USERRIGHTS T WHERE T.OPNO = ? AND T.DEPT_ID = ?) UR ");
		sql.append(" LEFT JOIN SYS_RESOURCES R ON UR.RESOURCEID = R.RID ");
		sql.append(" LEFT JOIN SYS_RESOURCES_BUTTON RB ON R.RID = RB.RID ");
		sql.append(" WHERE 1 = 1 ");

		String userId = userPO.getOpNo();
		String deptId = userPO.getOpType() == 1 ? ORG_ADMIN_DEPT : userPO.getDeptId();
		if (userPO.getOpType() < 2) {
			channelId = "";
		}

		List<Object> paramList = new ArrayList<Object>();
		paramList.add(userId);
		paramList.add(deptId);

		if (StrUtil.isNotBlank(channelId)) {
			sql.append(" AND R.APP_ID = ? ");
			paramList.add(channelId);
		}

		if (StrUtil.isNotBlank(channelRType)) {
			if (channelRType.indexOf(",") != -1) {
				String rtypes = StringUtil.getNewSplitedStr(channelRType, ",", ",", "'");
				sql.append(" AND R.CHANNEL_RTYPE IN ( " + rtypes + " ) ");
			} else {
				sql.append(" AND R.CHANNEL_RTYPE = ? ");
				paramList.add(channelRType);
			}
		}

		List<Map<String, Object>> queryResult = this.getSelectList(null, sql.toString(), paramList);

		return getResources(ResourceGroupType.SUBJECT, deptId, queryResult);
	}

	/**
	 * 获取[部门里的角色，属于部门的角色]授权的资源 @Title: getUserRoleInDeptResources @date 2018年4月24日 @author yanhang @Description: 获取[部门里的角色，属于部门的角色]授权的资源 @param userPO @param channelId @param channelRType @return @throws
	 */
	private Set<ResourceInfo> getUserRoleInDeptResources(UserPO userPO, String channelId, String channelRType) {
		StringBuffer sql = new StringBuffer(" SELECT RU.ROLEID RGROUP_ID, RU.OPNO, RU.DEPT_ID, R.RID, R.PARENTID, R.TITLE, R.ALLORDIDX, R.APP_ID, R.RLOGO, R.RTYPE, R.CHANNEL_RTYPE, RB.BVALUE ");
		sql.append(" FROM (SELECT T.OPNO, T.DEPT_ID, T.ROLEID FROM SYS_N_ROLEUSER T WHERE T.OPNO = ? AND T.DEPT_ID = ?) RU ");
		sql.append(" LEFT JOIN SYS_N_ROLERIGHTS RR ON RU.ROLEID = RR.ROLEID ");
		sql.append(" LEFT JOIN SYS_RESOURCES R ON RR.RESOURCEID = R.RID ");
		sql.append(" LEFT JOIN SYS_RESOURCES_BUTTON RB ON R.RID = RB.RID ");
		sql.append(" WHERE 1 = 1 ");

		String userId = userPO.getOpNo();
		String deptId = userPO.getOpType() == 1 ? ORG_ADMIN_DEPT : userPO.getDeptId();
		if (userPO.getOpType() < 2) {
			channelId = "";
		}

		List<Object> paramList = new ArrayList<Object>();
		paramList.add(userId);
		paramList.add(deptId);

		if (StrUtil.isNotBlank(channelId)) {
			sql.append(" AND R.APP_ID = ? ");
			paramList.add(channelId);
		}

		if (StrUtil.isNotBlank(channelRType)) {
			if (channelRType.indexOf(",") != -1) {
				String rtypes = StringUtil.getNewSplitedStr(channelRType, ",", ",", "'");
				sql.append(" AND R.CHANNEL_RTYPE IN ( " + rtypes + " ) ");
			} else {
				sql.append(" AND R.CHANNEL_RTYPE = ? ");
				paramList.add(channelRType);
			}
		}

		List<Map<String, Object>> queryResult = this.getSelectList(null, sql.toString(), paramList);

		return getResources(ResourceGroupType.ROLE_IN_DEPT, deptId, queryResult);
	}

	/**
	 * 获取[部门的角色，分配给部门的角色]授权的资源 @Title: getUserDeptRoleResources @date 2018年4月24日 @author yanhang @Description: 获取[部门的角色，分配给部门的角色]授权的资源 @param userPO @param channelId @param channelRType @return @throws
	 */
	private Set<ResourceInfo> getUserDeptRoleResources(UserPO userPO, String channelId, String channelRType) {
		StringBuffer sql = new StringBuffer(" SELECT RD.ROLEID RGROUP_ID, RD.DEPT_ID, R.RID, R.PARENTID, R.TITLE, R.ALLORDIDX, R.APP_ID, R.RLOGO, R.RTYPE, R.CHANNEL_RTYPE, RB.BVALUE ");
		sql.append(" FROM (SELECT T.DEPT_ID, T.ROLEID FROM SYS_N_ROLE_DEPARTMENT T WHERE T.DEPT_ID = ?) RD ");
		sql.append(" LEFT JOIN SYS_N_ROLERIGHTS RR ON RD.ROLEID = RR.ROLEID ");
		sql.append(" LEFT JOIN SYS_RESOURCES R ON RR.RESOURCEID = R.RID ");
		sql.append(" LEFT JOIN SYS_RESOURCES_BUTTON RB ON R.RID = RB.RID ");
		sql.append(" WHERE 1 = 1 ");

		String deptId = userPO.getOpType() == 1 ? ORG_ADMIN_DEPT : userPO.getDeptId();
		if (userPO.getOpType() < 2) {
			channelId = "";
		}

		List<Object> paramList = new ArrayList<Object>();
		paramList.add(deptId);

		if (StrUtil.isNotBlank(channelId)) {
			sql.append(" AND R.APP_ID = ? ");
			paramList.add(channelId);
		}

		if (StrUtil.isNotBlank(channelRType)) {
			if (channelRType.indexOf(",") != -1) {
				String rtypes = StringUtil.getNewSplitedStr(channelRType, ",", ",", "'");
				sql.append(" AND R.CHANNEL_RTYPE IN ( " + rtypes + " ) ");
			} else {
				sql.append(" AND R.CHANNEL_RTYPE = ? ");
				paramList.add(channelRType);
			}
		}

		List<Map<String, Object>> queryResult = this.getSelectList(null, sql.toString(), paramList);

		return getResources(ResourceGroupType.DEPT_ROLE, deptId, queryResult);
	}

	/**
	 * 获取[部门+职务，同时满足]授权的资源
	 * 
	 * @author yanhang
	 * @date 2018年4月24日
	 * @Description: 获取[部门+职务，同时满足]授权的资源
	 * @param userPO
	 * @param channelId
	 * @param channelRType
	 * @return @throws
	 */
	private Set<ResourceInfo> getUserDeptAndOfficeTypeResources(UserPO userPO, String channelId, String channelRType) {
		// String rgroupIdSql = convertSql.replace(convertSql.getStrJoinSQL("DOT.DEPT_TYPE", "_", "DOT.OFFICE_TYPE"));

		StringBuffer sql = new StringBuffer(" SELECT (" + ConvertSqlDefault.getStrJoinSQL("DOT.DEPT_TYPE", "'_'", "DOT.OFFICE_TYPE") + ") RGROUP_ID, RDO.ROLEID, RDO.DEPT_TYPE, RDO.OFFICE_TYPE, R.RID, R.PARENTID, R.TITLE, R.ALLORDIDX, R.APP_ID, R.RLOGO, R.RTYPE, R.CHANNEL_RTYPE, RB.BVALUE ");
		sql.append(" FROM (SELECT " + ConvertSqlDefault.isnullSQL("DTYPE.BVALUE", "''") + " DEPT_TYPE, " + ConvertSqlDefault.isnullSQL("OTYPE.OFFICE_TYPE ", "''") + " OFFICE_TYPE FROM ");
		sql.append(" 	(SELECT DT.DEPTID, DT.BVALUE FROM SYS_N_USER_DEPT_INFO UD, SYS_DEPARTMENT_TYPE DT WHERE UD.DEPT_ID = DT.DEPTID AND UD.DEPT_ID = ? AND UD.OPNO = ?) DTYPE  ");
		sql.append(" 	union ");
		sql.append(" 	(SELECT UDOT.DEPT_ID, UDOT.OFFICE_TYPE FROM SYS_N_USER_DEPT_OFFICE_TYPE UDOT WHERE UDOT.DEPT_ID = ? AND UDOT.OPNO = ?) OTYPE ");
		sql.append(" 	ON DTYPE.DEPTID = OTYPE.DEPT_ID) DOT ");
		sql.append(" LEFT JOIN SYS_N_ROLES_DEPT_OFFICE RDO ON (DOT.DEPT_TYPE = " + ConvertSqlDefault.isnullSQL("RDO.DEPT_TYPE", "''") + " AND DOT.OFFICE_TYPE = " + ConvertSqlDefault.isnullSQL("RDO.OFFICE_TYPE", "''") + ") ");
		sql.append(" LEFT JOIN SYS_N_ROLES_DEPT_OFFICE_RIGHTS RDOR ON RDO.ROLEID = RDOR.ROLEID ");
		sql.append(" LEFT JOIN SYS_RESOURCES R ON RDOR.RESOURCEID = R.RID ");
		sql.append(" LEFT JOIN SYS_RESOURCES_BUTTON RB ON R.RID = RB.RID ");
		sql.append(" WHERE RDO.ORGAN_ID = ? ");

		String userId = userPO.getOpNo();
		String deptId = userPO.getOpType() == 1 ? ORG_ADMIN_DEPT : userPO.getDeptId();
		if (userPO.getOpType() < 2) {
			channelId = "";
		}

		List<Object> paramList = new ArrayList<Object>();
		paramList.add(deptId);
		paramList.add(userId);
		paramList.add(deptId);
		paramList.add(userId);
		paramList.add(userPO.getOrgId());

		if (StrUtil.isNotBlank(channelId)) {
			sql.append(" AND R.APP_ID = ? ");
			paramList.add(channelId);
		}

		if (StrUtil.isNotBlank(channelRType)) {
			if (channelRType.indexOf(",") != -1) {
				String rtypes = StringUtil.getNewSplitedStr(channelRType, ",", ",", "'");
				sql.append(" AND R.CHANNEL_RTYPE IN ( " + rtypes + " ) ");
			} else {
				sql.append(" AND R.CHANNEL_RTYPE = ? ");
				paramList.add(channelRType);
			}
		}

		String _sql = sql.toString();
		List<Map<String, Object>> queryResult = new ArrayList<Map<String, Object>>();
		// try {
		// queryResult = this.getSelectList(null, _sql, paramList);
		// } catch (Exception e) {
		// log.info("获取[部门+职务]资源异常！" + e.getMessage() + System.lineSeparator() + " ConvertSql: " + sql.toString());
		// throw new RuntimeException(e);
		// }

		return getResources(ResourceGroupType.DEPTTYPE_OFFICETYPE, deptId, queryResult);
	}

	/**
	 * 整理结果集
	 * 
	 * @param resourceGroupType
	 * @param resourceGroupDepartment
	 * @param allResources
	 *            以资源ID分组，资源信息列表，需包含RID、RGROUP_ID、PARENTID、TITLE、ALLORDIDX、APP_ID、RLOGO、CHANNEL_RTYPE、BVALUE列
	 * @return
	 */
	private Set<ResourceInfo> getResources(ResourceGroupType resourceGroupType, String resourceGroupDepartment, List<Map<String, Object>> allResources) {
		Set<ResourceInfo> result = new HashSet<ResourceInfo>();

		Set<String> resourceIdsTmp = new HashSet<String>();
		for (Map<String, Object> resource : allResources) {
			String resourceId = resource.get("RID") == null ? "" : (String) resource.get("RID");
			if (!resourceIdsTmp.contains(resourceId)) {
				String resourceGroupId = resource.get("RGROUP_ID") == null ? "" : (String) resource.get("RGROUP_ID");
				String resourceParentId = resource.get("PARENTID") == null ? "" : (String) resource.get("PARENTID");
				String resourceTitle = resource.get("TITLE") == null ? "" : (String) resource.get("TITLE");
				String resourceAllordidx = resource.get("ALLORDIDX") == null ? "" : (String) resource.get("ALLORDIDX");
				String channelId = resource.get("APP_ID") == null ? "" : (String) resource.get("APP_ID");
				String powerCode = resource.get("RLOGO") == null ? "" : (String) resource.get("RLOGO");
				String type = resource.get("CHANNEL_RTYPE") == null ? "" : (String) resource.get("CHANNEL_RTYPE");
				List<String> buttons = getResourceButtons(allResources, resourceId);

				ResourceInfo resouce = new ResourceInfo();
				resouce.setResourceGroupType(resourceGroupType);
				resouce.setResourceGroupDepartment(resourceGroupDepartment);
				resouce.setResourceGroupId(resourceGroupId);
				resouce.setResourceGroupCode("");
				resouce.setResourceId(resourceId);
				resouce.setResourceParentId(resourceParentId);
				resouce.setResourceTitle(resourceTitle);
				resouce.setResourceAllordidx(resourceAllordidx);
				resouce.setResourcePowerCode(powerCode);
				resouce.setResourceChannelId(channelId);
				resouce.setResourceType(type);
				resouce.setResourceStatus("");
				resouce.setResourceButtons(buttons);
				result.add(resouce);

				resourceIdsTmp.add(resourceId);
			}
		}

		return result;
	}

	/**
	 * 获取资源的按钮列表
	 * 
	 * @param allResources
	 *            资源信息列表，需包含rid、rlogo和bvalue列
	 * @param resourceId
	 * @return
	 */
	private List<String> getResourceButtons(List<Map<String, Object>> allResources, String resourceId) {
		List<String> list = new ArrayList<String>();
		for (Map<String, Object> resource : allResources) {
			String _resourceId = (String) resource.get("RID");
			String _bvalue = resource.get("BVALUE") == null ? null : String.valueOf(resource.get("BVALUE"));
			if (_resourceId != null && _bvalue != null && _resourceId.equals(resourceId)) {
				if (checkButtonPermission(allResources, _resourceId, _bvalue)) {
					String powerCode = resource.get("RLOGO") == null ? "" : (String) resource.get("RLOGO");
					list.add(powerCode + "_" + _bvalue);
				}
			}
		}

		return list;
	}

	/**
	 * 判断是否有指定按钮权限<br>
	 * sys_resources_button表数据在入库时同步插入一条相应的数据到sys_resources表里，入库ID为rid+"_"+bvalue<br>
	 * 本方法判断指定按钮的rid+bvalue是否在sys_resources里存在，存在则认为有该按钮权限<br>
	 * 
	 * @param allResources
	 * @param resourceId
	 * @param bvalue
	 * @return
	 */
	private boolean checkButtonPermission(List<Map<String, Object>> allResources, String resourceId, String bvalue) {
		String buttonId = resourceId + "_" + bvalue;
		for (Map<String, Object> resources : allResources) {
			String buttonIdInResource = resources.get("RID") == null ? "" : (String) resources.get("RID");
			if (buttonIdInResource != null && buttonId.equals(buttonIdInResource)) {
				return true;
			}
		}

		return false;
	}

	/**
	 * 直接给用户授权
	 * 
	 * @param userId
	 * @param userDepartmentId
	 * @param resourceId
	 * @param creatorId
	 * @param creatorDepartmentId
	 * @return
	 */
	public ResultBean directlyUserAuthorization(String userId, String userDepartmentId, String resourceId, String creatorId, String creatorDepartmentId) {
		ResultBean rb = new ResultBean();

		try {
			List<Object> paramList = new ArrayList<Object>();
			StringBuffer sb = new StringBuffer();

			// 判断是否已存在授权记录
			sb = new StringBuffer(" SELECT U.PID FROM SYS_N_USERRIGHTS U WHERE U.OPNO = ? AND U.RESOURCEID = ? ");
			paramList.add(userId);
			paramList.add(resourceId);
			List<Map<String, Object>> list = this.getSelectList(null, sb.toString(), paramList);
			if (list != null && list.size() > 0) {
				rb.setSuccess(false);
				rb.setMsg("已存在授权记录，添加失败！");
				return rb;
			}

			// TODO 数据有效性判断

			// TODO 添加者权限判断，是否具有对目标用户进行resourceId授权的权限

			String id = Long.toString(IdGenerator.generateNewId());

			ConvertSql convertSql = new ConvertSql(this.getDbType());
			String createTime = convertSql.replace(convertSql.setDatetimeSQL(DateUtil.now()));

			String sql = String.format(" INSERT INTO SYS_N_USERRIGHTS VALUES (?, ?, ?, (SELECT opaccount FROM sys_n_users WHERE opno = ?), %s, ?, ?, ?) ", createTime);
			paramList = new ArrayList<Object>();
			paramList.add(id);
			paramList.add(userId);
			paramList.add(resourceId);
			paramList.add(creatorId);
			paramList.add(userDepartmentId);
			paramList.add(creatorId);
			paramList.add(creatorDepartmentId);

			int result = this.executeSql(null, sql, paramList);
			if (result == 0) {
				rb.setSuccess(false);
				rb.setMsg("添加授权记录失败！");
			}
		} catch (Exception e) {
			rb.setSuccess(false);
			rb.setMsg("添加授权记录异常！");
			log.info(rb.getMsg() + e.getMessage());
		}

		return rb;
	}

	/**
	 * 注入资源到资源表【注：调用时需纳入事务控制】
	 * 
	 * @param channelId
	 * @param resources
	 * @return
	 */
	public ResultBean injectResources(String channelId, Set<ResourceInfo> resources) {
		ResultBean rb = new ResultBean();

		try {
			// 过滤重复id的资源
			StringBuffer ids = new StringBuffer("");
			Map<String, ResourceInfo> map = new HashMap<String, ResourceInfo>();
			String resourceId = null;
			for (ResourceInfo resourceInfo : resources) {
				resourceId = resourceInfo.getResourceId();
				if (!map.containsKey(resourceId)) {
					map.put(resourceId, resourceInfo);

					ids.append("'").append(resourceId).append("',");
				}
			}

			if (map.size() == 0) {
				rb.setSuccess(false);
				rb.setMsg("资源注入数据为空，注入失败！");
				return rb;
			}

			String idsStr = ids.toString();
			idsStr = idsStr.substring(0, idsStr.lastIndexOf(","));

			// TODO SQL注入攻击过滤

			// 删除原有数据
			this.executeSql(null, " DELETE FROM SYS_RESOURCES WHERE APP_ID = '" + channelId + "' AND RID IN (" + idsStr + ") ", new ArrayList<Object>());

			List<Map<String, Object>> list = this.getSelectList(null, " SELECT RID FROM SYS_RESOURCES WHERE RID IN (" + idsStr + ") ", new ArrayList<Object>());
			if (list.size() > 0) {
				rb.setSuccess(false);
				rb.setMsg("资源ID已存在，资源注入失败！");
				// return rb;
				throw new RuntimeException(rb.getMsg());
			}

			// 入库
			StringBuffer sb = new StringBuffer();
			Iterator<ResourceInfo> it = map.values().iterator();
			List<String> batchInsertSqlArray = new ArrayList<String>();
			ResourceInfo info = null;
			while (it.hasNext()) {
				info = it.next();
				sb = new StringBuffer(" INSERT INTO SYS_RESOURCES (RID, PARENTID, TITLE, VISIBLE, APP_ID, CHANNEL_RTYPE) VALUES ");
				sb.append(String.format(" ( '%s', '%s', '%s', 1, '%s', '%s' ) ", info.getResourceId(), info.getResourceParentId(), info.getResourceTitle(), info.getResourceChannelId(), info.getResourceType()));
				batchInsertSqlArray.add(sb.toString());
			}

			String[] sqls = batchInsertSqlArray.toArray(new String[batchInsertSqlArray.size()]);
			int[] r = this.getJdbcTemplate(0).batchUpdate(sqls);
			if (r.length == 0) {
				rb.setSuccess(false);
				rb.setMsg("资源注入失败！");
			}
		} catch (Exception e) {
			rb.setSuccess(false);
			rb.setMsg("资源注入异常！" + e.getMessage());
			log.info(rb.getMsg() + e.getMessage());
			throw new RuntimeException(e.getMessage());
		}

		return rb;
	}

	/**
	 * 获取资源表数据，并以IDs对应的对象为根节点，组成树结构
	 * 
	 * @param cascade
	 *            0：仅获取ids对应的一级节点；1：获取ids一级节点以及一级子节点；2：获取ids一级节点以及级联获取子节点。不能为空
	 * @param channelId
	 *            渠道系统ID，不能为空
	 * @param rtype
	 *            资源类型，可为空
	 * @param ids
	 *            资源ID，格式：ID1|ID2|...，可为空
	 * @return
	 */
	public List<TreeNode> getResourceTreeByIds(String cascade, String channelId, String rtype, String ids) {
		Set<ResourceInfo> resources = getResourcesByIds(cascade, channelId, rtype, ids);

		List<ResourceInfo> list = new ArrayList<ResourceInfo>();
		list.addAll(resources);

		List<ResourceInfo> rootList = new ArrayList<ResourceInfo>();
		String[] ridsArray = ids.split("\\|");
		for (String rid : ridsArray) {
			for (ResourceInfo resourceInfo : list) {
				if (rid.equalsIgnoreCase(resourceInfo.getResourceId())) {
					rootList.add(resourceInfo);
				}
			}
		}

		// 若根节点为空则所有节点视为根节点
		if (rootList.isEmpty()) {
			rootList = list;
		}

		TreeService<ResourceInfo> ts = new TreeService<ResourceInfo>();
		List<TreeNode> roots = ts.getTreeNodeList(rootList);

		List<Tree> trees = ts.getTrees(roots, list);
		List<TreeNode> treeRoots = new ArrayList<TreeNode>();
		for (Tree tree : trees) {
			treeRoots.add(tree.getRoot());
		}

		return treeRoots;
	}

	/**
	 * 获取资源表数据
	 * 
	 * @param cascade
	 *            0：仅获取ids对应的一级节点；1：获取ids一级节点以及一级子节点；2：获取ids一级节点以及级联获取子节点。不能为空
	 * @param channelId
	 *            渠道系统ID，不能为空
	 * @param rtype
	 *            资源类型，可为空
	 * @param ids
	 *            资源ID，格式：ID1|ID2|...，可为空
	 * @return
	 */
	public Set<ResourceInfo> getResourcesByIds(String cascade, String channelId, String rtype, String ids) {
		Set<ResourceInfo> resources = new HashSet<ResourceInfo>();

		if (StrUtil.isBlank(cascade) || StrUtil.isBlank(channelId)) {
			return resources;
		}

		StringBuffer idsb = new StringBuffer("");
		String[] ridsArray = ids.split("\\|");
		for (String rid : ridsArray) {
			if (StrUtil.isNotBlank(rid)) {
				idsb.append("'").append(rid).append("',");
			}
		}
		ids = idsb.toString();
		if (StrUtil.isNotBlank(ids)) {
			ids = ids.substring(0, ids.lastIndexOf(","));
		}

		List<Object> paramList = new ArrayList<Object>();
		StringBuffer sb = new StringBuffer(" SELECT R.RID, R.PARENTID, R.TITLE, R.ALLORDIDX, R.APP_ID, R.RLOGO, R.RTYPE, R.CHANNEL_RTYPE ");
		if ("0".equals(cascade)) {
			sb.append(" FROM SYS_RESOURCES R WHERE R.APP_ID = ? ");
			paramList.add(channelId);

			if (StrUtil.isNotBlank(rtype)) {
				sb.append(" AND R.CHANNEL_RTYPE = ? ");
				paramList.add(rtype);
			}

			if (StrUtil.isNotBlank(ids)) {
				sb.append(" AND R.RID IN ( " + ids + " ) ");
			}
			sb.append(" order by  ALLORDIDX,RID");
			List<Map<String, Object>> queryResult = this.getSelectList(null, sb.toString(), paramList);
			resources = getResources(null, "", queryResult);
		} else if ("1".equals(cascade)) {
			sb.append(" FROM SYS_RESOURCES R WHERE R.APP_ID = ? ");

			paramList.add(channelId);
			if (StrUtil.isNotBlank(ids)) {
				sb.append(" AND (R.RID IN ( " + ids + " ) OR R.PARENTID IN ( " + ids + " )) ");
			}
			if (StrUtil.isNotBlank(rtype)) {
				sb.append(" AND R.CHANNEL_RTYPE = ? ");
				paramList.add(rtype);
			}

			sb.append(" order by  ALLORDIDX,RID");
			List<Map<String, Object>> queryResult = this.getSelectList(null, sb.toString(), paramList);
			resources = getResources(null, "", queryResult);
		} else if ("2".equals(cascade)) {
			sb.append(" FROM SYS_RESOURCES R  WHERE R.APP_ID = ? ");
			paramList.add(channelId);
			if (StrUtil.isNotBlank(rtype)) {
				sb.append(" AND R.CHANNEL_RTYPE = ? ");
				paramList.add(rtype);
			}
			sb.append(" order by  ALLORDIDX,RID");
			// 获取APP_ID下的所有资源
			List<Map<String, Object>> queryResult = this.getSelectList(null, sb.toString(), paramList);
			Set<ResourceInfo> channelResources = getResources(null, "", queryResult);

			// set转list
			List<ResourceInfo> list = new ArrayList<ResourceInfo>();
			list.addAll(channelResources);

			// 获取根节点
			List<ResourceInfo> rootList = new ArrayList<ResourceInfo>();
			for (String rid : ridsArray) {
				for (ResourceInfo resourceInfo : list) {
					if (rid.equalsIgnoreCase(resourceInfo.getResourceId())) {
						rootList.add(resourceInfo);
					}
				}
			}

			// 若根节点为空则所有节点视为根节点
			if (rootList.isEmpty()) {
				rootList = list;
			}

			// 由根节点构建树
			TreeService<ResourceInfo> ts = new TreeService<ResourceInfo>();
			List<TreeNode> roots = ts.getTreeNodeList(rootList);
			List<Tree> trees = ts.getTrees(roots, list);

			// 获取参与树的节点
			List<TreeNode> nodes = new ArrayList<TreeNode>(); // 参与到树的所有节点
			for (Tree tree : trees) {
				nodes = tree.getNodes();
				nodes.add(tree.getRoot()); // 把根节点算上
				for (TreeNode treeNode : nodes) {
					for (ResourceInfo resourceInfo : channelResources) {
						if (treeNode.getId().equalsIgnoreCase(resourceInfo.getResourceId())) {
							if (StrUtil.isNotBlank(rtype)) {
								if (rtype.equalsIgnoreCase(resourceInfo.getResourceType())) {
									resources.add(resourceInfo);
								}
							} else {
								resources.add(resourceInfo);
							}
						}
					}
				}

				nodes.clear();
			}
		}

		return resources;
	}

	/**
	 * 级联删除资源
	 * 
	 * @param channelId
	 *            不能为空
	 * @param rtype
	 *            可为空
	 * @param ids
	 *            id1|id2|...，可为空
	 * @param protectLocalResource
	 * @param isHardDelete
	 *            是否硬删除，若非硬删除，则有子节点的不能删除
	 * @return
	 */
	public ResultBean deleteResources(String channelId, String rtype, String ids, boolean isHardDelete) {
		if (!isHardDelete) {
			String _ids = StringUtil.getNewSplitedStr(ids, "\\|", ",", "'");
			String _sql = " SELECT COUNT(1) FROM SYS_RESOURCES WHERE APP_ID = ? AND PARENTID IN ( " + _ids + " ) ";
			List<Object> _paramList = new ArrayList<Object>();
			_paramList.add(channelId);
			if (((BigDecimal) getOneFiledValue(null, _sql, _paramList)).intValue() > 0) {
				return new ResultBean(false, "待删除节点包含子节点，不能直接删除！");
			}
		}

		return deleteResources(channelId, rtype, ids);
	}

	/**
	 * 级联删除资源【注：硬删除，谨慎操作】
	 * 
	 * @param channelId
	 *            不能为空
	 * @param rtype
	 *            可为空
	 * @param ids
	 *            id1|id2|...，可为空
	 * @param protectLocalResource
	 * @return
	 */
	public ResultBean deleteResources(String channelId, String rtype, String ids) {
		ResultBean rb = new ResultBean();

		if (StrUtil.isBlank(channelId)) {
			return new ResultBean(false, "APPID不能为空");
		}

		try {
			List<Object> paramList = new ArrayList<Object>();
			StringBuffer sb = new StringBuffer(" DELETE FROM SYS_RESOURCES WHERE APP_ID = ? ");
			paramList.add(channelId);

			if (StrUtil.isNotBlank(rtype)) {
				sb.append(" AND CHANNEL_RTYPE = ? ");
				paramList.add(rtype);
			}

			if (StrUtil.isNotBlank(ids)) {
				Set<ResourceInfo> resources = getResourcesByIds("2", channelId, rtype, ids);
				StringBuffer idsb = new StringBuffer("");
				for (ResourceInfo resourceInfo : resources) {
					idsb.append("'").append(resourceInfo.getNodeId()).append("',");
				}
				ids = idsb.toString();
				if (ids.indexOf(",") != -1) {
					ids = ids.substring(0, ids.lastIndexOf(","));
				}

				if (StrUtil.isNotBlank(ids)) {
					sb.append(" AND RID IN ( " + ids + " ) ");
				}
			}

			this.executeSql(null, sb.toString(), paramList);

			// TODO 删除与之关联的表记录

		} catch (Exception e) {
			rb.setSuccess(false);
			rb.setMsg("级联删除资源异常！" + e.getMessage());
			log.info(rb.getMsg() + e.getMessage());
			throw new RuntimeException(e.getMessage());
		}

		return rb;
	}

}
