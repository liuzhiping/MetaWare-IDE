#ifndef __posix_h__
#define __posix_h__ 1
/*HEADER*******************************************************************
***************************************************************************
***
*** Copyright (c) 1989-2004 ARC International.
***
*** All rights reserved
***
*** This software embodies materials and concepts which are confidential
*** to ARC International and is made available
*** solely pursuant to the terms of a written license agreement with
*** ARC International
***
*** File: posix.h
***
*** Comments: 
*** 
***    This include file provides defines to allow for MQX2.40 functions to
***  be called with POSIX compliant names.
***
***************************************************************************
*END**********************************************************************/

#define errno        _task_errno

#define EOK          MQX_EOK         
#define E2BIG        MQX_E2BIG       
#define EACCES       MQX_EACCES      
#define EAGAIN       MQX_EAGAIN      
#define EBADF        MQX_EBADF       
#define EBADMSG      MQX_EBADMSG     
#define EBUSY        MQX_EBUSY       
#define ECANCELED    MQX_ECANCELED   
#define ECHILD       MQX_ECHILD      
#define EDEADLK      MQX_EDEADLK     
#define EDOM         MQX_EDOM        
#define EEXIST       MQX_EEXIST      
#define EFAULT       MQX_EFAULT      
#define EFBIG        MQX_EFBIG       
#define EINPROGRESS  MQX_EINPROGRESS 
#define EINTR        MQX_EINTR       
#define EINVAL       MQX_EINVAL      
#define EIO          MQX_EIO         
#define EISDIR       MQX_EISDIR      
#define EMFILE       MQX_EMFILE      
#define EMLINK       MQX_EMLINK      
#define EMSGSIZE     MQX_EMSGSIZE    
#define ENAMETOOLONG MQX_ENAMETOOLONG
#define ENFILE       MQX_ENFILE      
#define ENODEV       MQX_ENODEV      
#define ENOENT       MQX_ENOENT      
#define ENOEXEC      MQX_ENOEXEC     
#define ENOLCK       MQX_ENOLCK      
#define ENOMEM       MQX_ENOMEM      
#define ENOSPC       MQX_ENOSPC      
#define ENOSYS       MQX_ENOSYS      
#define ENOTDIR      MQX_ENOTDIR     
#define ENOTEMPTY    MQX_ENOTEMPTY   
#define ENOTSUP      MQX_ENOTSUP     
#define ENOTTY       MQX_ENOTTY      
#define ENXIO        MQX_ENXIO       
#define EPERM        MQX_EPERM       
#define EPIPE        MQX_EPIPE       
#define ERANGE       MQX_ERANGE      
#define EROFS        MQX_EROFS       
#define ESPIPE       MQX_ESPIPE      
#define ESRCH        MQX_ESRCH       
#define ETIMEDOUT    MQX_ETIMEDOUT   
#define EXDEV        MQX_EXDEV       

#define pthread_mutex_create_component     _mutex_create_component
#define pthread_mutex_destroy              _mutex_destroy
#define pthread_mutex_getprioceiling       _mutex_get_max_priority
#define pthread_mutex_setprioceiling       _mutex_set_max_priority
#define pthread_mutex_init                 _mutex_init
#define pthread_mutex_lock                 _mutex_lock
#define pthread_mutex_try_lock             _mutex_try_lock
#define pthread_mutex_unlock               _mutex_unlock
#define pthread_mutex_task_wait_count      _mutex_get_wait_count
#define pthread_mutexattr_destroy          _mutatr_destroy
#define pthread_mutexattr_getlimitspin     _mutatr_get_spin_limit
#define pthread_mutexattr_setlimitspin     _mutatr_set_spin_limit
#define pthread_mutexattr_getprioceiling   _mutatr_get_max_priority
#define pthread_mutexattr_setprioceiling   _mutatr_set_max_priority
#define pthread_mutexattr_getprotocol      _mutatr_get_protocol
#define pthread_mutexattr_setprotocol      _mutatr_set_protocol
#define pthread_mutexattr_getwaitingpolicy _mutatr_get_policy
#define pthread_mutexattr_setwaitingpolicy _mutatr_set_policy
#define pthread_mutexattr_init             _mutatr_init

#define pthread_mutex_t                    MUTEX_STRUCT
#define pthread_mutex_attr_t               MUTEX_ATTR_STRUCT

#endif
/* EOF */
