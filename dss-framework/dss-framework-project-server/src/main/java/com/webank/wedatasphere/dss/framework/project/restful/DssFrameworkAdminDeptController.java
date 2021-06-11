package com.webank.wedatasphere.dss.framework.project.restful;


import com.webank.wedatasphere.dss.framework.admin.common.constant.UserConstants;
import com.webank.wedatasphere.dss.framework.admin.common.domain.Message;
import com.webank.wedatasphere.dss.framework.admin.common.utils.StringUtils;
import com.webank.wedatasphere.dss.framework.admin.pojo.entity.DssAdminDept;
import com.webank.wedatasphere.dss.framework.admin.service.DssAdminDeptService;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;

import javax.annotation.Resource;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;


/**
 * <p>
 * 部门表 前端控制器
 * </p>
 *
 * @author Lvjw
 * @since 2021-06-01
 */
//@RestController
//@RequestMapping("/dss/framework/admin/dept")

@Component
@Path("/dss/framework/admin/dept")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class DssFrameworkAdminDeptController {
    @Resource
    private DssAdminDeptService dssAdminDeptService;


    @GET
    @Path("/list")
    public Message listAll(DssAdminDept dept) {
        List<DssAdminDept> list = dssAdminDeptService.selectDeptList(dept);
        return Message.ok().data("deptList", list).message("成功");
    }

    @POST
    public Message add(@RequestBody DssAdminDept dssAdminDept) {

        if (UserConstants.NOT_UNIQUE.equals(dssAdminDeptService.checkDeptNameUnique(dssAdminDept))) {
            return Message.error().message("新增部门'" + dssAdminDept.getDeptName() + "'失败，部门名称已存在");
        }

        int saveResult = dssAdminDeptService.insertDept(dssAdminDept);
        System.out.println(saveResult);
        if (saveResult >= 1) {
            return Message.ok().message("保存成功");
        } else {
            return Message.error().message("保存失败");
        }
    }

    /**
     * 获取部门下拉树列表
     */
//    @GetMapping("/treeselect")
    @GET
    @Path("/treeselect")
    public Message treeselect(DssAdminDept dept) {
        List<DssAdminDept> depts = dssAdminDeptService.selectDeptList(dept);
        return Message.ok().data("deptTree", dssAdminDeptService.buildDeptTreeSelect(depts)).message("树形部门获取成功");
    }


    //    @GetMapping(value = "/{deptId}")
    @GET
    @Path("/{deptId}")
    public Message getInfo(@PathParam("deptId") Long deptId) {
        return Message.ok().data("deptInfo", dssAdminDeptService.selectDeptById(deptId));
    }

    //    @PostMapping("/edit")
    @POST
    @Path("/edit")
    public Message edit(@Validated @RequestBody DssAdminDept dept) {
        if (UserConstants.NOT_UNIQUE.equals(dssAdminDeptService.checkDeptNameUnique(dept))) {
            return Message.error().message("修改部门'" + dept.getDeptName() + "'失败，部门名称已存在");
        } else if (dept.getParentId().equals(dept.getId())) {
            return Message.error().message("修改部门'" + dept.getDeptName() + "'失败，上级部门不能是自己");
        } else if (StringUtils.equals(UserConstants.DEPT_DISABLE, dept.getStatus())
                && dssAdminDeptService.selectNormalChildrenDeptById(dept.getId()) > 0) {
            return Message.error().message("该部门包含未停用的子部门！");
        }

        return Message.ok().data("修改成功", dssAdminDeptService.updateDept(dept));
    }

    /**
     * 删除部门
     */
    @POST
    @Path("/{deptId}")
    public Message remove(@PathParam("deptId") Long deptId) {
        if (dssAdminDeptService.hasChildById(deptId)) {
            return Message.error().message("存在下级部门,不允许删除");
        }
        if (dssAdminDeptService.checkDeptExistUser(deptId)) {
            return Message.error().message("部门存在用户,不允许删除");
        }
        return Message.ok().data("删除成功", dssAdminDeptService.deleteDeptById(deptId));
    }


}
