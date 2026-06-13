package com.quantalabs.jamusync.dao;

import com.quantalabs.jamusync.database.DatabaseManager;
import com.quantalabs.jamusync.model.Transaction;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class TransactionDAO {

    /**
     * Get the most recent transactions overall.
     * @param limit Number of transactions to return.
     * @return List of transactions.
     */
    public List<Transaction> getRecentTransactions(int limit) {
        List<Transaction> transactions = new ArrayList<>();
        String sql = "SELECT t.*, u.username AS recorded_by_username FROM transactions t " +
                     "LEFT JOIN users u ON t.recorded_by = u.id " +
                     "ORDER BY t.created_at DESC LIMIT ?";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, limit);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Transaction tx = mapResultSetToTransaction(rs);
                    tx.setRecordedByUsername(rs.getString("recorded_by_username"));
                    transactions.add(tx);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return transactions;
    }

    /**
     * Get the most recent transactions recorded by a specific staff user.
     * @param staffUserId Staff user identifier.
     * @param limit Number of transactions to return.
     * @return List of transactions.
     */
    public List<Transaction> getRecentTransactionsByStaff(int staffUserId, int limit) {
        List<Transaction> transactions = new ArrayList<>();
        String sql = "SELECT t.*, u.username AS recorded_by_username FROM transactions t " +
                     "LEFT JOIN users u ON t.recorded_by = u.id " +
                     "WHERE t.recorded_by = ? " +
                     "ORDER BY t.created_at DESC LIMIT ?";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, staffUserId);
            pstmt.setInt(2, limit);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Transaction tx = mapResultSetToTransaction(rs);
                    tx.setRecordedByUsername(rs.getString("recorded_by_username"));
                    transactions.add(tx);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return transactions;
    }

    /**
     * Get total sales revenue completed today.
     * @return revenue.
     */
    public double getTodaySalesTotal() {
        String sql = "SELECT SUM(total) FROM transactions WHERE status = 'Completed' AND date(created_at) = date('now', 'localtime')";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            if (rs.next()) {
                return rs.getDouble(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0.0;
    }

    /**
     * Get count of transactions completed today.
     * @return count.
     */
    public int getTodaySalesCount() {
        String sql = "SELECT COUNT(*) FROM transactions WHERE status = 'Completed' AND date(created_at) = date('now', 'localtime')";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * Get pending transactions count.
     * @return count.
     */
    public int getPendingTransactionsCount() {
        String sql = "SELECT COUNT(*) FROM transactions WHERE status = 'Pending'";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * Get total sales revenue completed today by a specific staff.
     * @param staffUserId Staff user identifier.
     * @return revenue.
     */
    public double getTodaySalesTotalByStaff(int staffUserId) {
        String sql = "SELECT SUM(total) FROM transactions WHERE status = 'Completed' AND recorded_by = ? AND date(created_at) = date('now', 'localtime')";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, staffUserId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0.0;
    }

    /**
     * Get count of transactions completed today by a specific staff.
     * @param staffUserId Staff user identifier.
     * @return count.
     */
    public int getTodaySalesCountByStaff(int staffUserId) {
        String sql = "SELECT COUNT(*) FROM transactions WHERE status = 'Completed' AND recorded_by = ? AND date(created_at) = date('now', 'localtime')";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, staffUserId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * Insert a transaction and return the generated ID within a connection.
     */
    public int insertTransactionReturningId(Connection conn, Transaction tx) throws SQLException {
        String sql = "INSERT INTO transactions (order_type, buyer_name, voucher_id, subtotal, discount, total, status, recorded_by) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, tx.getOrderType());
            pstmt.setString(2, tx.getBuyerName());
            if (tx.getVoucherId() != null && tx.getVoucherId() > 0) {
                pstmt.setInt(3, tx.getVoucherId());
            } else {
                pstmt.setNull(3, java.sql.Types.INTEGER);
            }
            pstmt.setDouble(4, tx.getSubtotal());
            pstmt.setDouble(5, tx.getDiscount());
            pstmt.setDouble(6, tx.getTotal());
            pstmt.setString(7, tx.getStatus());
            pstmt.setInt(8, tx.getRecordedBy());

            pstmt.executeUpdate();
            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return -1;
    }

    /**
     * Update transaction status.
     */
    public boolean updateStatus(int transactionId, String status) {
        String sql = "UPDATE transactions SET status = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, status);
            pstmt.setInt(2, transactionId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Get transactions with optional filters.
     */
    public List<Transaction> getFilteredTransactions(String status, String startDate, String endDate) {
        List<Transaction> transactions = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
            "SELECT t.*, u.username AS recorded_by_username FROM transactions t " +
            "LEFT JOIN users u ON t.recorded_by = u.id WHERE 1=1"
        );
        List<Object> params = new ArrayList<>();

        if (status != null && !status.isEmpty() && !"All".equalsIgnoreCase(status)) {
            sql.append(" AND t.status = ?");
            params.add(status);
        }
        if (startDate != null && !startDate.isEmpty()) {
            sql.append(" AND date(t.created_at) >= date(?)");
            params.add(startDate);
        }
        if (endDate != null && !endDate.isEmpty()) {
            sql.append(" AND date(t.created_at) <= date(?)");
            params.add(endDate);
        }
        sql.append(" ORDER BY t.created_at DESC");

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                pstmt.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Transaction tx = mapResultSetToTransaction(rs);
                    tx.setRecordedByUsername(rs.getString("recorded_by_username"));
                    transactions.add(tx);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return transactions;
    }

    /**
     * Get a single transaction by ID.
     */
    public Transaction getTransactionById(int id) {
        String sql = "SELECT t.*, u.username AS recorded_by_username FROM transactions t " +
                     "LEFT JOIN users u ON t.recorded_by = u.id WHERE t.id = ?";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    Transaction tx = mapResultSetToTransaction(rs);
                    tx.setRecordedByUsername(rs.getString("recorded_by_username"));
                    return tx;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Get total revenue for completed transactions in date range.
     */
    public double getRevenueByDateRange(String startDate, String endDate) {
        String sql = "SELECT SUM(total) FROM transactions WHERE status = 'Completed' " +
                     "AND date(created_at) >= date(?) AND date(created_at) <= date(?)";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, startDate);
            pstmt.setString(2, endDate);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble(1);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0.0;
    }

    /**
     * Insert a transaction (useful for tests/seeding).
     * @param tx The transaction.
     * @return True if insert succeeded.
     */
    public boolean insertTransaction(Transaction tx) {
        StringBuilder sql = new StringBuilder("INSERT INTO transactions (order_type, buyer_name, voucher_id, subtotal, discount, total, status, recorded_by");
        if (tx.getCreatedAt() != null) {
            sql.append(", created_at");
        }
        if (tx.getUpdatedAt() != null) {
            sql.append(", updated_at");
        }
        sql.append(") VALUES (?, ?, ?, ?, ?, ?, ?, ?");
        if (tx.getCreatedAt() != null) {
            sql.append(", ?");
        }
        if (tx.getUpdatedAt() != null) {
            sql.append(", ?");
        }
        sql.append(")");

        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {
            
            pstmt.setString(1, tx.getOrderType());
            pstmt.setString(2, tx.getBuyerName());
            if (tx.getVoucherId() != null && tx.getVoucherId() > 0) {
                pstmt.setInt(3, tx.getVoucherId());
            } else {
                pstmt.setNull(3, java.sql.Types.INTEGER);
            }
            pstmt.setDouble(4, tx.getSubtotal());
            pstmt.setDouble(5, tx.getDiscount());
            pstmt.setDouble(6, tx.getTotal());
            pstmt.setString(7, tx.getStatus());
            pstmt.setInt(8, tx.getRecordedBy());
            
            int paramIndex = 9;
            if (tx.getCreatedAt() != null) {
                pstmt.setString(paramIndex++, tx.getCreatedAt());
            }
            if (tx.getUpdatedAt() != null) {
                pstmt.setString(paramIndex++, tx.getUpdatedAt());
            }
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private Transaction mapResultSetToTransaction(ResultSet rs) throws SQLException {
        Integer voucherIdVal = rs.getInt("voucher_id");
        if (rs.wasNull()) {
            voucherIdVal = null;
        }
        return new Transaction(
            rs.getInt("id"),
            rs.getString("order_type"),
            rs.getString("buyer_name"),
            voucherIdVal,
            rs.getDouble("subtotal"),
            rs.getDouble("discount"),
            rs.getDouble("total"),
            rs.getString("status"),
            rs.getInt("recorded_by"),
            rs.getString("created_at"),
            rs.getString("updated_at")
        );
    }
}
